package nl.rijksoverheid.moz.notificatie.event

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.cloudevents.FbsEventTypes
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.notificatie.service.NotificatieService
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.jboss.logging.Logger

@ApplicationScoped
class BerichtOntvangenConsumer(
    private val notificatieService: NotificatieService,
    private val objectMapper: ObjectMapper
) {

    private val log = Logger.getLogger(BerichtOntvangenConsumer::class.java)

    @Incoming("bericht-ontvangen")
    fun onBerichtOntvangen(event: CloudEvent) {
        if (event.type != FbsEventTypes.BERICHT_ONTVANGEN) {
            log.warnf("Onverwacht event type: %s, verwacht: %s", event.type, FbsEventTypes.BERICHT_ONTVANGEN)
            return
        }

        val data = event.data ?: run {
            log.warn("CloudEvent bevat geen data")
            return
        }

        val bericht = try {
            objectMapper.readValue(data.toBytes(), Bericht::class.java)
        } catch (e: JsonProcessingException) {
            log.errorf(e, "Ongeldige JSON in bericht-ontvangen event: id=%s", event.id)
            throw e
        } catch (e: IllegalArgumentException) {
            log.errorf(e, "Ongeldige data in bericht-ontvangen event: id=%s", event.id)
            throw e
        }

        notificatieService.verwerkBerichtOntvangen(bericht)
    }
}
