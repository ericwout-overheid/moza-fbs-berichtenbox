package nl.rijksoverheid.moz.cloudevents

import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Builder voor FBS CloudEvents conform CloudEvents spec 1.0 en NL GOV profiel.
 *
 * Stelt automatisch de verplichte attributen in:
 * - specversion: 1.0
 * - id: UUID
 * - time: RFC 3339 timestamp
 * - datacontenttype: application/json
 */
object FbsCloudEventBuilder {

    private const val DATA_CONTENT_TYPE = "application/json"

    /**
     * Bouwt een CloudEvent met de opgegeven attributen.
     *
     * @param source URN van de bron (gebruik [FbsSourceUrn.create])
     * @param type event type (gebruik [FbsEventTypes] constanten)
     * @param subject optioneel onderwerp (bijv. bericht-ID)
     * @param data optionele event data als bytes
     * @return het gebouwde [CloudEvent]
     */
    fun build(
        source: URI,
        type: String,
        subject: String? = null,
        data: ByteArray? = null
    ): CloudEvent {
        val builder = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(source)
            .withType(type)
            .withTime(OffsetDateTime.now())
            .withDataContentType(DATA_CONTENT_TYPE)

        subject?.let { builder.withSubject(it) }
        data?.let { builder.withData(DATA_CONTENT_TYPE, it) }

        return builder.build()
    }
}
