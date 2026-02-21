package nl.rijksoverheid.moz.ldv

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LdvLoggerTest {

    private val spanExporter = InMemorySpanExporter.create()

    private val openTelemetry: OpenTelemetrySdk = OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build()
        )
        .build()

    private val logger = LdvLogger.create(openTelemetry)

    @Test
    fun `logVerwerking maakt een afgeronde span`() {
        logger.logVerwerking(createVerwerking())

        val spans = spanExporter.finishedSpanItems
        assertEquals(1, spans.size)
        assertEquals("BerichtOpslaan", spans[0].name)
    }

    @Test
    fun `logVerwerking span bevat LDV attributen`() {
        val verwerking = createVerwerking()
        logger.logVerwerking(verwerking)

        val span = spanExporter.finishedSpanItems[0]
        assertEquals(
            verwerking.verwerkingsActiviteitId.toString(),
            span.attributes.get(AttributeKey.stringKey("dpl.core.processing_activity_id"))
        )
        assertEquals(
            verwerking.betrokkeneId,
            span.attributes.get(AttributeKey.stringKey("dpl.core.data_subject_id"))
        )
    }

    @Test
    fun `withinVerwerking retourneert resultaat van blok`() {
        val result = logger.withinVerwerking(createVerwerking()) {
            "test resultaat"
        }

        assertEquals("test resultaat", result)
    }

    @Test
    fun `withinVerwerking maakt een span aan`() {
        logger.withinVerwerking(createVerwerking()) { "ok" }

        val spans = spanExporter.finishedSpanItems
        assertEquals(1, spans.size)
        assertEquals("BerichtOpslaan", spans[0].name)
    }

    @Test
    fun `withinVerwerking registreert fout op span bij exception`() {
        assertFailsWith<RuntimeException> {
            logger.withinVerwerking(createVerwerking()) {
                throw RuntimeException("test fout")
            }
        }

        val spans = spanExporter.finishedSpanItems
        assertEquals(1, spans.size)
        assertEquals(StatusCode.ERROR, spans[0].status.statusCode)
        assertTrue(spans[0].events.any { it.name == "exception" })
    }

    @Test
    fun `withinVerwerking gooit exception door naar aanroeper`() {
        val exception = assertFailsWith<IllegalStateException> {
            logger.withinVerwerking(createVerwerking()) {
                throw IllegalStateException("verwachte fout")
            }
        }

        assertEquals("verwachte fout", exception.message)
    }

    @Test
    fun `withinVerwerking sluit span bij exception`() {
        assertFailsWith<RuntimeException> {
            logger.withinVerwerking(createVerwerking()) {
                throw RuntimeException("fout")
            }
        }

        // Span moet beëindigd zijn (anders zou het niet in finishedSpanItems staan)
        assertEquals(1, spanExporter.finishedSpanItems.size)
    }

    private fun createVerwerking() = LdvVerwerking(
        verwerkingsActiviteitId = URI.create("urn:nl:fbs:verwerking:bericht-opslaan"),
        betrokkeneId = "123456789",
        betrokkeneIdType = "BSN",
        operatieNaam = "BerichtOpslaan"
    )
}
