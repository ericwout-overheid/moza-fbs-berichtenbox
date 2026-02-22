package nl.rijksoverheid.moz.notificatie.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.transaction.Transactional
import jakarta.transaction.Transactional.TxType.REQUIRES_NEW
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.util.PiiMasker
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import nl.rijksoverheid.moz.notificatie.event.NotificatieEventPublisher
import nl.rijksoverheid.moz.notificatie.mapping.NotificatieMapper
import nl.rijksoverheid.moz.notificatie.repository.NotificatieRepository
import org.jboss.logging.Logger

/**
 * Aparte bean voor het verzenden van notificaties zodat [Transactional] met [REQUIRES_NEW]
 * correct werkt. CDI interceptors worden alleen geactiveerd via de proxy, en self-invocation
 * binnen dezelfde bean omzeilt de proxy — waardoor @Transactional genegeerd wordt.
 *
 * Wordt aangeroepen door [NotificatieService.verwerkBerichtOntvangen] voor elk kanaal.
 */
@ApplicationScoped
class NotificatieVerzendService(
    private val notificatieRepository: NotificatieRepository,
    private val verzenders: Instance<NotificatieVerzender>,
    private val eventPublisher: NotificatieEventPublisher
) {

    private val log = Logger.getLogger(NotificatieVerzendService::class.java)

    @Transactional(REQUIRES_NEW)
    fun verzendNotificatie(bericht: Bericht, kanaal: NotificatieKanaal, adres: String) {
        val entity = NotificatieEntity(
            ontvangerIdType = bericht.ontvangerIdType,
            ontvangerId = bericht.ontvangerId,
            kanaal = kanaal,
            onderwerp = bericht.onderwerp,
            inhoud = bericht.inhoud,
            status = NotificatieStatusWaarde.AANGEMAAKT
        )
        notificatieRepository.bewaar(entity)

        val verzender = verzenders.stream()
            .filter { it.kanaal == kanaal }
            .findFirst()
            .orElseThrow { IllegalStateException("Geen verzender gevonden voor kanaal $kanaal") }

        try {
            verzender.verzend(adres, bericht.onderwerp, bericht.inhoud)
        } catch (e: Exception) {
            log.errorf(e, "Notificatie verzenden mislukt: kanaal=%s, ontvanger=%s", kanaal, PiiMasker.mask(bericht.ontvangerId))
            entity.markeerMislukt(e.message ?: "Onbekende fout")
            notificatieRepository.bewaar(entity)
            return
        }

        entity.markeerVerzonden()
        notificatieRepository.bewaar(entity)
        eventPublisher.publishNotificatieVerzonden(NotificatieMapper.toDto(entity))
    }
}
