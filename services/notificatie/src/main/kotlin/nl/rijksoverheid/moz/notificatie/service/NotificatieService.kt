package nl.rijksoverheid.moz.notificatie.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.Notificatie
import nl.rijksoverheid.moz.common.model.NotificatieStatus
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.NotificatieVerzoek
import nl.rijksoverheid.moz.common.util.PiiMasker
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.ldv.LdvVerwerking
import nl.rijksoverheid.moz.notificatie.client.NotificatieprofielClient
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import nl.rijksoverheid.moz.notificatie.exception.NotificatieNietGevondenException
import nl.rijksoverheid.moz.notificatie.mapping.NotificatieMapper
import nl.rijksoverheid.moz.notificatie.repository.NotificatieRepository
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger
import java.net.URI
import java.util.UUID

/**
 * Service voor het aanmaken, opvragen en verwerken van notificaties.
 * Verwerkt inkomende bericht-ontvangen events via [verwerkBerichtOntvangen].
 * Het daadwerkelijk verzenden is gedelegeerd aan [NotificatieVerzendService].
 */
@ApplicationScoped
class NotificatieService(
    private val notificatieRepository: NotificatieRepository,
    @param:RestClient private val profielClient: NotificatieprofielClient,
    private val ldvLogger: LdvLogger,
    private val verzendService: NotificatieVerzendService
) {

    private val log = Logger.getLogger(NotificatieService::class.java)

    @Transactional
    fun maakNotificatie(verzoek: NotificatieVerzoek): Notificatie {
        val entity = NotificatieEntity(
            ontvangerIdType = verzoek.ontvangerIdType,
            ontvangerId = verzoek.ontvangerId,
            kanaal = verzoek.kanaal,
            onderwerp = verzoek.onderwerp,
            inhoud = verzoek.inhoud,
            status = NotificatieStatusWaarde.AANGEMAAKT
        )

        return ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/notificatie-aanmaken"),
                betrokkeneId = verzoek.ontvangerId,
                betrokkeneIdType = verzoek.ontvangerIdType.name,
                operatieNaam = "maakNotificatie"
            )
        ) {
            notificatieRepository.bewaar(entity)
            NotificatieMapper.toDto(entity)
        }
    }

    /**
     * Haal de status op van een notificatie.
     *
     * LDV-logging gebeurt best-effort: een fout in de logging mag het ophalen van
     * de status niet blokkeren. Dit is conform het LDV-principe dat logging nooit
     * de primaire functionaliteit mag hinderen.
     */
    @Transactional
    fun haalStatus(notificatieId: UUID): NotificatieStatus {
        val entity = notificatieRepository.vindOpId(notificatieId)
            ?: throw NotificatieNietGevondenException(notificatieId)

        try {
            ldvLogger.logVerwerking(
                LdvVerwerking(
                    verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/notificatie-status-ophalen"),
                    betrokkeneId = entity.ontvangerId,
                    betrokkeneIdType = entity.ontvangerIdType.name,
                    operatieNaam = "haalStatus"
                )
            )
        } catch (e: Exception) {
            // Breed catch-blok is intentioneel: LDV-logging is best-effort en mag de primaire functionaliteit niet blokkeren
            log.errorf(e, "LDV logging mislukt voor haalStatus: notificatieId=%s", notificatieId)
        }

        return NotificatieMapper.toStatusDto(entity)
    }

    @Transactional
    fun verwerkBerichtOntvangen(bericht: Bericht) {
        val profiel = try {
            profielClient.haalProfiel(bericht.ontvangerId, bericht.ontvangerIdType)
        } catch (e: WebApplicationException) {
            if (e.response.status == 404) {
                log.infof("Geen profiel gevonden voor ontvanger %s (type %s), notificatie overgeslagen",
                    PiiMasker.mask(bericht.ontvangerId), bericht.ontvangerIdType)
                return
            }
            throw e
        } catch (e: ProcessingException) {
            log.errorf(e, "Verbinding met notificatieprofiel-service mislukt voor ontvanger %s",
                PiiMasker.mask(bericht.ontvangerId))
            throw e
        }

        val emailAdres = profiel.emailAdres
        if (profiel.emailNotificaties && !emailAdres.isNullOrBlank()) {
            verzendService.verzendNotificatie(bericht, NotificatieKanaal.EMAIL, emailAdres)
        } else if (profiel.emailNotificaties) {
            log.warnf("E-mailnotificaties ingeschakeld maar emailAdres is leeg: ontvanger=%s (type=%s)",
                PiiMasker.mask(bericht.ontvangerId), bericht.ontvangerIdType)
        }

        val telefoonnummer = profiel.telefoonnummer
        if (profiel.smsNotificaties && !telefoonnummer.isNullOrBlank()) {
            verzendService.verzendNotificatie(bericht, NotificatieKanaal.SMS, telefoonnummer)
        } else if (profiel.smsNotificaties) {
            log.warnf("SMS-notificaties ingeschakeld maar telefoonnummer is leeg: ontvanger=%s (type=%s)",
                PiiMasker.mask(bericht.ontvangerId), bericht.ontvangerIdType)
        }
    }
}
