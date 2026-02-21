package nl.rijksoverheid.moz.notificatie.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.ldv.LdvConfiguration
import nl.rijksoverheid.moz.ldv.LdvLogger
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class LdvProducer(
    @param:ConfigProperty(name = "otel.exporter.otlp.endpoint", defaultValue = "http://localhost:4317")
    private val otlpEndpoint: String
) {

    @Produces
    @ApplicationScoped
    fun ldvLogger(): LdvLogger {
        val openTelemetry = LdvConfiguration.create(otlpEndpoint)
        return LdvLogger.create(openTelemetry)
    }
}
