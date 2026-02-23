package nl.rijksoverheid.moz.berichtenmagazijn.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.ldv.LdvConfiguration
import nl.rijksoverheid.moz.ldv.LdvLogger
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

/** CDI producer for LdvLogger. Vereist omdat fbs-ldv een plain library is zonder Quarkus auto-configuratie. */
@ApplicationScoped
class LdvProducer(
    @param:ConfigProperty(name = "otel.exporter.otlp.endpoint")
    private val otlpEndpoint: String
) {
    private val log = Logger.getLogger(LdvProducer::class.java)

    @Produces
    @ApplicationScoped
    fun ldvLogger(): LdvLogger {
        val openTelemetry = LdvConfiguration.create(otlpEndpoint)
        log.infof("LDV geconfigureerd met OTLP endpoint: %s", otlpEndpoint)
        // TODO: Voeg OTLP export-fout monitoring toe (LDV-specificatie verbiedt dataverlies)
        return LdvLogger.create(openTelemetry)
    }
}
