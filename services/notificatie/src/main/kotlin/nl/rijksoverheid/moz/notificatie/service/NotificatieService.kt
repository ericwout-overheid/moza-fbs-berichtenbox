package nl.rijksoverheid.moz.notificatie.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.transaction.Transactional
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.Notificatie
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatus
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.NotificatieVerzoek
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
import java.time.Instant
import java.util.UUID

@ApplicationScoped
@Transactional
class NotificatieService(
    private val notificatieRepository: NotificatieRepository,
    private val verzenders: Instance<NotificatieVerzender>,
    @param:RestClient private val profielClient: NotificatieprofielClient,
    private val ldvLogger: LdvLogger
) {

    private val log = Logger.getLogger(NotificatieService::class.java)

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
            log.errorf(e, "LDV logging mislukt voor haalStatus: notificatieId=%s", notificatieId)
        }

        return NotificatieMapper.toStatusDto(entity)
    }

    fun verwerkBerichtOntvangen(bericht: Bericht) {
        val profiel = try {
            profielClient.haalProfiel(bericht.ontvangerId, bericht.ontvangerIdType)
        } catch (e: Exception) {
            log.warnf("Profiel ophalen mislukt voor ontvanger %s (type %s), notificatie overgeslagen: %s",
                bericht.ontvangerId, bericht.ontvangerIdType, e.message)
            return
        }

        val emailAdres = profiel.emailAdres
        if (profiel.emailNotificaties && !emailAdres.isNullOrBlank()) {
            verzendNotificatie(bericht, NotificatieKanaal.EMAIL, emailAdres)
        }

        val telefoonnummer = profiel.telefoonnummer
        if (profiel.smsNotificaties && !telefoonnummer.isNullOrBlank()) {
            verzendNotificatie(bericht, NotificatieKanaal.SMS, telefoonnummer)
        }
    }

    private fun verzendNotificatie(bericht: Bericht, kanaal: NotificatieKanaal, adres: String) {
        val entity = NotificatieEntity(
            ontvangerIdType = bericht.ontvangerIdType,
            ontvangerId = bericht.ontvangerId,
            kanaal = kanaal,
            onderwerp = bericht.onderwerp,
            inhoud = bericht.inhoud,
            status = NotificatieStatusWaarde.AANGEMAAKT
        )
        notificatieRepository.bewaar(entity)

        try {
            val verzender = verzenders.stream()
                .filter { it.kanaal == kanaal }
                .findFirst()
                .orElseThrow { IllegalStateException("Geen verzender gevonden voor kanaal $kanaal") }

            verzender.verzend(adres, bericht.onderwerp, bericht.inhoud)

            entity.status = NotificatieStatusWaarde.VERZONDEN
            entity.verzondenOp = Instant.now()
        } catch (e: Exception) {
            log.errorf(e, "Notificatie verzenden mislukt: kanaal=%s, ontvanger=%s", kanaal, bericht.ontvangerId)
            entity.status = NotificatieStatusWaarde.MISLUKT
            entity.foutmelding = e.message ?: "Onbekende fout"
        }

        notificatieRepository.bewaar(entity)
    }
}
