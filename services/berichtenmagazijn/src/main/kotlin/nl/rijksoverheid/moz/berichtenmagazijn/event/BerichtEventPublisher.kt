package nl.rijksoverheid.moz.berichtenmagazijn.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.cloudevents.FbsCloudEventBuilder
import nl.rijksoverheid.moz.cloudevents.FbsEventTypes
import nl.rijksoverheid.moz.cloudevents.FbsSourceUrn
import nl.rijksoverheid.moz.common.model.Bericht
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.jboss.logging.Logger
import java.net.URI

@ApplicationScoped
class BerichtEventPublisher(
    @param:Channel("bericht-ontvangen") private val ontvangenEmitter: Emitter<CloudEvent>,
    @param:Channel("bericht-gelezen") private val gelezenEmitter: Emitter<CloudEvent>,
    @param:Channel("bericht-verwijderd") private val verwijderdEmitter: Emitter<CloudEvent>,
    private val objectMapper: ObjectMapper
) {

    private val log = Logger.getLogger(BerichtEventPublisher::class.java)

    fun publishBerichtOntvangen(afzenderOin: String, bericht: Bericht) {
        safePublish(FbsEventTypes.BERICHT_ONTVANGEN, bericht.id.toString()) {
            val event = buildEvent(afzenderOin, FbsEventTypes.BERICHT_ONTVANGEN, bericht)
            sendEvent(ontvangenEmitter, event, FbsEventTypes.BERICHT_ONTVANGEN, bericht.id.toString())
        }
    }

    fun publishBerichtGelezen(afzenderOin: String, bericht: Bericht) {
        safePublish(FbsEventTypes.BERICHT_GELEZEN, bericht.id.toString()) {
            val event = buildEvent(afzenderOin, FbsEventTypes.BERICHT_GELEZEN, bericht)
            sendEvent(gelezenEmitter, event, FbsEventTypes.BERICHT_GELEZEN, bericht.id.toString())
        }
    }

    fun publishBerichtVerwijderd(afzenderOin: String, berichtId: java.util.UUID) {
        safePublish(FbsEventTypes.BERICHT_VERWIJDERD, berichtId.toString()) {
            val source = FbsSourceUrn.create(afzenderOin, "berichtenmagazijn")
            val event = FbsCloudEventBuilder.build(
                source = source,
                type = FbsEventTypes.BERICHT_VERWIJDERD,
                subject = berichtId.toString()
            )
            sendEvent(verwijderdEmitter, event, FbsEventTypes.BERICHT_VERWIJDERD, berichtId.toString())
        }
    }

    private fun safePublish(type: String, subject: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            log.errorf(e, "CloudEvent bouwen/publiceren mislukt: type=%s, subject=%s", type, subject)
        }
    }

    private fun sendEvent(emitter: Emitter<CloudEvent>, event: CloudEvent, type: String, subject: String) {
        emitter.send(event).whenComplete { _, throwable ->
            if (throwable != null) {
                log.errorf(
                    throwable,
                    "Kafka event publicatie mislukt: type=%s, subject=%s",
                    type, subject
                )
            }
        }
    }

    private fun buildEvent(afzenderOin: String, type: String, bericht: Bericht): CloudEvent {
        val source = FbsSourceUrn.create(afzenderOin, "berichtenmagazijn")
        val data = objectMapper.writeValueAsBytes(bericht)
        return FbsCloudEventBuilder.build(
            source = source,
            type = type,
            subject = bericht.id.toString(),
            dataschema = BERICHT_SCHEMA,
            data = data
        )
    }

    companion object {
        private val BERICHT_SCHEMA = URI.create("https://berichtenmagazijn.fbs.moza.nl/openapi.json#/components/schemas/Bericht")
    }
}
