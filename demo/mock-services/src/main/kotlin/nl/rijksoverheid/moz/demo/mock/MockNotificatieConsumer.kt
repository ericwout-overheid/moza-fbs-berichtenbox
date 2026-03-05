package nl.rijksoverheid.moz.demo.mock

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.util.PiiMasker
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.jboss.logging.Logger
import java.util.concurrent.CopyOnWriteArrayList

@ApplicationScoped
class MockNotificatieConsumer(
    private val ssePublisher: SseEventPublisher,
    private val objectMapper: ObjectMapper
) {
    private val log = Logger.getLogger(MockNotificatieConsumer::class.java)
    private val notificaties = CopyOnWriteArrayList<DemoEvent>()

    @Incoming("bericht-ontvangen")
    fun onBerichtOntvangen(event: CloudEvent) {
        try {
            val data = event.data?.toBytes()
            val bericht = if (data != null) {
                objectMapper.readValue(data, Bericht::class.java)
            } else null

            val maskedOntvanger = bericht?.let { PiiMasker.mask(it.ontvangerId) } ?: "onbekend"
            val onderwerp = bericht?.onderwerp ?: "onbekend"

            log.infof("Notificatie voor bericht: ontvanger=%s, onderwerp=%s",
                maskedOntvanger, onderwerp)

            val demoEvent = DemoEvent(
                type = "NOTIFICATIE_VERSTUURD",
                afzender = bericht?.afzenderOin,
                ontvanger = maskedOntvanger,
                onderwerp = onderwerp,
                detail = "Notificatie verstuurd aan ${bericht?.ontvangerIdType} $maskedOntvanger"
            )

            notificaties.add(demoEvent)
            if (notificaties.size > 100) {
                notificaties.removeAt(0)
            }
            ssePublisher.publish(demoEvent)
        } catch (e: Exception) {
            log.errorf(e, "Fout bij verwerken bericht-ontvangen event")
            ssePublisher.publish(
                DemoEvent(
                    type = "NOTIFICATIE_ERROR",
                    detail = "Fout bij verwerken event: ${e.message}"
                )
            )
        }
    }

    fun recenteNotificaties(): List<DemoEvent> = notificaties.toList()
}
