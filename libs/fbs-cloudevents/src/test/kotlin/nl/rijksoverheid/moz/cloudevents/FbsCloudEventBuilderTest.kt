package nl.rijksoverheid.moz.cloudevents

import io.cloudevents.CloudEvent
import io.cloudevents.SpecVersion
import java.net.URI
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FbsCloudEventBuilderTest {

    private val source = URI.create("urn:nld:oin:00000001234567890123:systeem:test")

    @Test
    fun `specversion is 1_0`() {
        val event = buildEvent()
        assertEquals(SpecVersion.V1, event.specVersion)
    }

    @Test
    fun `id is een geldig UUID`() {
        val event = buildEvent()
        assertNotNull(UUID.fromString(event.id))
    }

    @Test
    fun `source is correct`() {
        val event = buildEvent()
        assertEquals(source, event.source)
    }

    @Test
    fun `type is correct`() {
        val event = buildEvent()
        assertEquals(FbsEventTypes.BERICHT_ONTVANGEN, event.type)
    }

    @Test
    fun `time is ingesteld`() {
        val event = buildEvent()
        assertNotNull(event.time)
    }

    @Test
    fun `datacontenttype is application_json`() {
        val event = buildEvent()
        assertEquals("application/json", event.dataContentType)
    }

    @Test
    fun `subject wordt ingesteld indien opgegeven`() {
        val event = FbsCloudEventBuilder.build(
            source = source,
            type = FbsEventTypes.BERICHT_ONTVANGEN,
            subject = "bericht-123"
        )
        assertEquals("bericht-123", event.subject)
    }

    @Test
    fun `subject is null indien niet opgegeven`() {
        val event = buildEvent()
        assertNull(event.subject)
    }

    @Test
    fun `data wordt ingesteld indien opgegeven`() {
        val data = """{"berichtId": "123"}""".toByteArray()
        val event = FbsCloudEventBuilder.build(
            source = source,
            type = FbsEventTypes.BERICHT_ONTVANGEN,
            data = data
        )
        assertNotNull(event.data)
    }

    private fun buildEvent(): CloudEvent = FbsCloudEventBuilder.build(
        source = source,
        type = FbsEventTypes.BERICHT_ONTVANGEN
    )
}
