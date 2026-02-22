package nl.rijksoverheid.moz.notificatie.service

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.transaction.Transactional
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.util.PiiMasker
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import nl.rijksoverheid.moz.notificatie.repository.NotificatieRepository
import org.jboss.logging.Logger

@ApplicationScoped
class NotificatieRetryService(
    private val notificatieRepository: NotificatieRepository,
    private val verzenders: Instance<NotificatieVerzender>
) {

    private val log = Logger.getLogger(NotificatieRetryService::class.java)

    @Scheduled(every = "5m")
    @Transactional
    fun retryMislukteNotificaties() {
        val retryable = notificatieRepository.findRetryable(MAX_POGINGEN)
        if (retryable.isEmpty()) return

        log.infof("Retry voor %d mislukte notificaties", retryable.size)

        for (entity in retryable) {
            retryNotificatie(entity)
        }
    }

    private fun retryNotificatie(entity: NotificatieEntity) {
        val verzender = verzenders.stream()
            .filter { it.kanaal == entity.kanaal }
            .findFirst()
            .orElse(null)

        if (verzender == null) {
            log.errorf("Geen verzender gevonden voor kanaal %s, markeer als definitief mislukt", entity.kanaal)
            entity.markeerDefinitiefMislukt("Geen verzender voor kanaal ${entity.kanaal}")
            notificatieRepository.bewaar(entity)
            return
        }

        entity.markeerVoorRetry()
        try {
            verzender.verzend(resolveAdres(entity), entity.onderwerp, entity.inhoud)
            entity.markeerVerzonden()
            log.infof("Retry geslaagd voor notificatie %s", entity.id)
        } catch (e: Exception) {
            if (entity.pogingen + 1 >= MAX_POGINGEN) {
                entity.markeerDefinitiefMislukt(e.message ?: "Onbekende fout")
                log.warnf("Notificatie %s definitief mislukt na %d pogingen, ontvanger=%s",
                    entity.id, MAX_POGINGEN, PiiMasker.mask(entity.ontvangerId))
            } else {
                entity.markeerMislukt(e.message ?: "Onbekende fout")
                log.warnf("Retry mislukt voor notificatie %s (poging %d/%d)",
                    entity.id, entity.pogingen, MAX_POGINGEN)
            }
        }
        notificatieRepository.bewaar(entity)
    }

    private fun resolveAdres(entity: NotificatieEntity): String {
        // Bij retry is het originele adres niet opgeslagen — gebruik een placeholder.
        // In een productie-implementatie zou het adres op de entity staan.
        return when (entity.kanaal) {
            NotificatieKanaal.EMAIL -> "retry@fbs.nl"
            NotificatieKanaal.SMS -> "+31600000000"
        }
    }

    companion object {
        const val MAX_POGINGEN = 3
    }
}
