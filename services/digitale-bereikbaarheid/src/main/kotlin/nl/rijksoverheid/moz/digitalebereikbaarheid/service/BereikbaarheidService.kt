package nl.rijksoverheid.moz.digitalebereikbaarheid.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import nl.rijksoverheid.moz.common.model.Bereikbaarheid
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.digitalebereikbaarheid.entity.BereikbaarheidEntity
import nl.rijksoverheid.moz.digitalebereikbaarheid.exception.BereikbaarheidNietGevondenException
import nl.rijksoverheid.moz.digitalebereikbaarheid.mapping.BereikbaarheidMapper
import nl.rijksoverheid.moz.digitalebereikbaarheid.repository.BereikbaarheidRepository
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.ldv.LdvVerwerking
import nl.rijksoverheid.moz.common.util.PiiMasker
import org.jboss.logging.Logger
import java.net.URI
import java.time.Instant

@ApplicationScoped
class BereikbaarheidService(
    private val bereikbaarheidRepository: BereikbaarheidRepository,
    private val ldvLogger: LdvLogger
) {

    private val log = Logger.getLogger(BereikbaarheidService::class.java)

    @Transactional
    fun haalBereikbaarheid(ontvangerId: String, ontvangerIdType: OntvangerIdType): Bereikbaarheid {
        val entity = bereikbaarheidRepository.vindOpOntvanger(ontvangerId, ontvangerIdType)
            ?: throw BereikbaarheidNietGevondenException(ontvangerId, ontvangerIdType)

        try {
            ldvLogger.logVerwerking(
                LdvVerwerking(
                    verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bereikbaarheid-ophalen"),
                    betrokkeneId = ontvangerId,
                    betrokkeneIdType = ontvangerIdType.name,
                    operatieNaam = "haalBereikbaarheid"
                )
            )
        } catch (e: Exception) {
            // Breed catch-blok is intentioneel: LDV-logging is best-effort en mag de primaire functionaliteit niet blokkeren
            log.errorf(e, "LDV logging mislukt voor haalBereikbaarheid: ontvangerId=%s", PiiMasker.mask(ontvangerId))
        }

        return BereikbaarheidMapper.toDto(entity)
    }

    @Transactional
    fun registreerBereikbaarheid(
        ontvangerId: String,
        ontvangerIdType: OntvangerIdType,
        bereikbaarheid: Bereikbaarheid
    ): Bereikbaarheid {
        require(bereikbaarheid.ontvangerId == ontvangerId && bereikbaarheid.ontvangerIdType == ontvangerIdType) {
            "ontvangerId en ontvangerIdType in body moeten overeenkomen met pad-parameters"
        }

        // withinVerwerking is bewust: schrijfoperaties moeten auditeerbaar zijn via LDV.
        // Als LDV-logging faalt, mag de schrijfoperatie niet doorgaan.
        return ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bereikbaarheid-registreren"),
                betrokkeneId = ontvangerId,
                betrokkeneIdType = ontvangerIdType.name,
                operatieNaam = "registreerBereikbaarheid"
            )
        ) {
            val existing = bereikbaarheidRepository.vindOpOntvanger(ontvangerId, ontvangerIdType)

            if (existing != null) {
                existing.digitaalBereikbaar = bereikbaarheid.digitaalBereikbaar
                existing.intrekkingsDatum = if (bereikbaarheid.digitaalBereikbaar) null else Instant.now()
                bereikbaarheidRepository.bewaar(existing)
                BereikbaarheidMapper.toDto(existing)
            } else {
                val entity = BereikbaarheidEntity(
                    ontvangerId = ontvangerId,
                    ontvangerIdType = ontvangerIdType,
                    digitaalBereikbaar = bereikbaarheid.digitaalBereikbaar,
                    intrekkingsDatum = if (bereikbaarheid.digitaalBereikbaar) null else Instant.now()
                )
                bereikbaarheidRepository.bewaar(entity)
                BereikbaarheidMapper.toDto(entity)
            }
        }
    }
}
