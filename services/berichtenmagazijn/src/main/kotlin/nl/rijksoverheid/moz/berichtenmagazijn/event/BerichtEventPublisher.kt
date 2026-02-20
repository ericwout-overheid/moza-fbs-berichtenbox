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

@ApplicationScoped
class BerichtEventPublisher(
    @param:Channel("bericht-ontvangen") private val ontvangenEmitter: Emitter<CloudEvent>,
    @param:Channel("bericht-gelezen") private val gelezenEmitter: Emitter<CloudEvent>,
    @param:Channel("bericht-verwijderd") private val verwijderdEmitter: Emitter<CloudEvent>,
    private val objectMapper: ObjectMapper
) {

    fun publishBerichtOntvangen(afzenderOin: String, bericht: Bericht) {
        val event = buildEvent(afzenderOin, FbsEventTypes.BERICHT_ONTVANGEN, bericht)
        ontvangenEmitter.send(event)
    }

    fun publishBerichtGelezen(afzenderOin: String, bericht: Bericht) {
        val event = buildEvent(afzenderOin, FbsEventTypes.BERICHT_GELEZEN, bericht)
        gelezenEmitter.send(event)
    }

    fun publishBerichtVerwijderd(afzenderOin: String, berichtId: java.util.UUID) {
        val source = FbsSourceUrn.create(afzenderOin, "berichtenmagazijn")
        val event = FbsCloudEventBuilder.build(
            source = source,
            type = FbsEventTypes.BERICHT_VERWIJDERD,
            subject = berichtId.toString()
        )
        verwijderdEmitter.send(event)
    }

    private fun buildEvent(afzenderOin: String, type: String, bericht: Bericht): CloudEvent {
        val source = FbsSourceUrn.create(afzenderOin, "berichtenmagazijn")
        val data = objectMapper.writeValueAsBytes(bericht)
        return FbsCloudEventBuilder.build(
            source = source,
            type = type,
            subject = bericht.id.toString(),
            data = data
        )
    }
}
