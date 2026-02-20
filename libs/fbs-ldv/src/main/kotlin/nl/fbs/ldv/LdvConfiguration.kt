package nl.fbs.ldv

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler

/**
 * Configuratie van de OpenTelemetry SDK voor het Logboek Dataverwerkingen.
 *
 * LET OP: Sampling is VERBODEN per LDV-specificatie.
 * Alle dataverwerkingen MOETEN zonder uitzondering worden gelogd.
 */
object LdvConfiguration {

    private const val DEFAULT_OTLP_ENDPOINT = "http://localhost:4317"

    /**
     * Maakt een geconfigureerde OpenTelemetry SDK instantie aan.
     *
     * @param otlpEndpoint het OTLP gRPC endpoint (standaard localhost:4317)
     * @return geconfigureerde [OpenTelemetrySdk] instantie
     */
    fun create(otlpEndpoint: String = DEFAULT_OTLP_ENDPOINT): OpenTelemetrySdk {
        val spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .build()

        val tracerProvider = SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOn())
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .build()

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
    }
}
