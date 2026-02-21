package nl.rijksoverheid.moz.notificatie.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.cloudevents.FbsCloudEventBuilder
import nl.rijksoverheid.moz.cloudevents.FbsEventTypes
import nl.rijksoverheid.moz.cloudevents.FbsSourceUrn
import nl.rijksoverheid.moz.common.model.Notificatie
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.jboss.logging.Logger

@ApplicationScoped
class NotificatieEventPublisher(
    @param:Channel("notificatie-verzonden") private val verzondenEmitter: Emitter<CloudEvent>,
    private val objectMapper: ObjectMapper
) {

    private val log = Logger.getLogger(NotificatieEventPublisher::class.java)

    fun publishNotificatieVerzonden(notificatie: Notificatie) {
        safePublish(FbsEventTypes.NOTIFICATIE_VERZONDEN, notificatie.id.toString()) {
            val source = FbsSourceUrn.create(SYSTEM_OIN, "notificatie")
            val data = objectMapper.writeValueAsBytes(notificatie)
            val event = FbsCloudEventBuilder.build(
                source = source,
                type = FbsEventTypes.NOTIFICATIE_VERZONDEN,
                subject = notificatie.id.toString(),
                data = data
            )
            verzondenEmitter.send(event).whenComplete { _, throwable ->
                if (throwable != null) {
                    log.errorf(throwable, "Kafka event publicatie mislukt: type=%s, subject=%s",
                        FbsEventTypes.NOTIFICATIE_VERZONDEN, notificatie.id)
                }
            }
        }
    }

    private fun safePublish(type: String, subject: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            log.errorf(e, "CloudEvent bouwen/publiceren mislukt: type=%s, subject=%s", type, subject)
        }
    }

    companion object {
        private const val SYSTEM_OIN = "00000001234567890000"
    }
}
