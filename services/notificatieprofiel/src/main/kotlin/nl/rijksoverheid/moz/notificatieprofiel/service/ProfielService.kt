package nl.rijksoverheid.moz.notificatieprofiel.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.ldv.LdvVerwerking
import nl.rijksoverheid.moz.notificatieprofiel.entity.ProfielEntity
import nl.rijksoverheid.moz.notificatieprofiel.exception.ProfielNietGevondenException
import nl.rijksoverheid.moz.notificatieprofiel.mapping.ProfielMapper
import nl.rijksoverheid.moz.notificatieprofiel.repository.ProfielRepository
import org.jboss.logging.Logger
import java.net.URI

/**
 * Service voor het beheren van notificatieprofielen van ontvangers.
 */
@ApplicationScoped
class ProfielService(
    private val profielRepository: ProfielRepository,
    private val ldvLogger: LdvLogger
) {

    private val log = Logger.getLogger(ProfielService::class.java)

    /**
     * Haal het notificatieprofiel op voor een ontvanger.
     *
     * LDV-logging gebeurt best-effort: een fout in de logging mag het ophalen van
     * het profiel niet blokkeren. Dit is conform het LDV-principe dat logging nooit
     * de primaire functionaliteit mag hinderen.
     */
    @Transactional
    fun haalProfiel(ontvangerId: String, ontvangerIdType: OntvangerIdType): Profiel {
        val entity = profielRepository.vindOpOntvanger(ontvangerId, ontvangerIdType)
            ?: throw ProfielNietGevondenException(ontvangerId, ontvangerIdType)

        try {
            ldvLogger.logVerwerking(
                LdvVerwerking(
                    verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/profiel-ophalen"),
                    betrokkeneId = ontvangerId,
                    betrokkeneIdType = ontvangerIdType.name,
                    operatieNaam = "haalProfiel"
                )
            )
        } catch (e: Exception) {
            // Breed catch-blok is intentioneel: LDV-logging is best-effort en mag de primaire functionaliteit niet blokkeren
            log.errorf(e, "LDV logging mislukt voor haalProfiel: ontvangerId=%s", ontvangerId)
        }

        return ProfielMapper.toDto(entity)
    }

    @Transactional
    fun werkProfielBij(ontvangerId: String, ontvangerIdType: OntvangerIdType, profiel: Profiel): Profiel {
        require(profiel.ontvangerId == ontvangerId && profiel.ontvangerIdType == ontvangerIdType) {
            "ontvangerId en ontvangerIdType in body moeten overeenkomen met pad-parameters"
        }

        return ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/profiel-bijwerken"),
                betrokkeneId = ontvangerId,
                betrokkeneIdType = ontvangerIdType.name,
                operatieNaam = "werkProfielBij"
            )
        ) {
            val existing = profielRepository.vindOpOntvanger(ontvangerId, ontvangerIdType)

            if (existing != null) {
                ProfielMapper.updateEntity(existing, profiel)
                profielRepository.bewaar(existing)
                ProfielMapper.toDto(existing)
            } else {
                val entity = ProfielEntity(
                    ontvangerId = ontvangerId,
                    ontvangerIdType = ontvangerIdType,
                    emailNotificaties = profiel.emailNotificaties,
                    smsNotificaties = profiel.smsNotificaties,
                    emailAdres = profiel.emailAdres,
                    telefoonnummer = profiel.telefoonnummer,
                    frequentie = profiel.frequentie
                )
                profielRepository.bewaar(entity)
                ProfielMapper.toDto(entity)
            }
        }
    }
}
