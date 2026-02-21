package nl.rijksoverheid.moz.ldv

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class LdvSpanBuilderTest {

    private val spanExporter = InMemorySpanExporter.create()

    private val openTelemetry: OpenTelemetrySdk = OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build()
        )
        .build()

    private val spanBuilder = LdvSpanBuilder.create(openTelemetry)

    @Test
    fun `span bevat processing_activity_id attribuut`() {
        val verwerking = createVerwerking()
        val span = spanBuilder.startSpan(verwerking)
        span.end()

        val spans = spanExporter.finishedSpanItems
        assertEquals(1, spans.size)
        assertEquals(
            verwerking.verwerkingsActiviteitId.toString(),
            spans[0].attributes.get(AttributeKey.stringKey("dpl.core.processing_activity_id"))
        )
    }

    @Test
    fun `span bevat data_subject_id attribuut`() {
        val verwerking = createVerwerking()
        val span = spanBuilder.startSpan(verwerking)
        span.end()

        val spans = spanExporter.finishedSpanItems
        assertEquals(
            verwerking.betrokkeneId,
            spans[0].attributes.get(AttributeKey.stringKey("dpl.core.data_subject_id"))
        )
    }

    @Test
    fun `span bevat data_subject_id_type attribuut`() {
        val verwerking = createVerwerking()
        val span = spanBuilder.startSpan(verwerking)
        span.end()

        val spans = spanExporter.finishedSpanItems
        assertEquals(
            verwerking.betrokkeneIdType,
            spans[0].attributes.get(AttributeKey.stringKey("dpl.core.data_subject_id_type"))
        )
    }

    @Test
    fun `span naam is operatienaam`() {
        val verwerking = createVerwerking()
        val span = spanBuilder.startSpan(verwerking)
        span.end()

        val spans = spanExporter.finishedSpanItems
        assertEquals(verwerking.operatieNaam, spans[0].name)
    }

    private fun createVerwerking() = LdvVerwerking(
        verwerkingsActiviteitId = URI.create("urn:nl:fbs:verwerking:bericht-opslaan"),
        betrokkeneId = "123456789",
        betrokkeneIdType = "BSN",
        operatieNaam = "BerichtOpslaan"
    )
}
