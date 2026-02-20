package nl.rijksoverheid.moz.ldv

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler

/**
 * Configuratie van de OpenTelemetry SDK voor het Logboek Dataverwerkingen.
 *
 * LET OP: Sampling is VERBODEN per LDV-specificatie.
 * Alle dataverwerkingen MOETEN zonder uitzondering worden gelogd.
 * SimpleSpanProcessor wordt gebruikt om te garanderen dat spans niet
 * stilzwijgend worden weggegooid (zoals bij BatchSpanProcessor kan gebeuren).
 */
object LdvConfiguration {

    private const val DEFAULT_OTLP_ENDPOINT = "http://localhost:4317"

    /**
     * Maakt een geconfigureerde OpenTelemetry SDK instantie aan.
     *
     * @param otlpEndpoint het OTLP gRPC endpoint (standaard localhost:4317)
     * @return geconfigureerde [OpenTelemetrySdk] instantie
     * @throws IllegalArgumentException als het endpoint leeg of ongeldig is
     */
    fun create(otlpEndpoint: String = DEFAULT_OTLP_ENDPOINT): OpenTelemetrySdk {
        require(otlpEndpoint.isNotBlank()) { "OTLP endpoint mag niet leeg zijn" }
        require(otlpEndpoint.startsWith("http://") || otlpEndpoint.startsWith("https://")) {
            "OTLP endpoint moet een geldig HTTP(S) URL zijn: $otlpEndpoint"
        }

        val spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .build()

        val tracerProvider = SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOn())
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build()

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
    }
}
