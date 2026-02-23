package nl.rijksoverheid.moz.berichtenmagazijn.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.ldv.LdvConfiguration
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.ldv.LdvPseudonimiseerder
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

/** CDI producer voor LdvLogger en LdvPseudonimiseerder. Vereist omdat fbs-ldv een plain library is zonder Quarkus auto-configuratie. */
@ApplicationScoped
class LdvProducer(
    @param:ConfigProperty(name = "otel.exporter.otlp.endpoint")
    private val otlpEndpoint: String,
    @param:ConfigProperty(name = "fbs.ldv.pseudonimisering.zout")
    private val pseudonimiseringZout: String
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

    @Produces
    @ApplicationScoped
    fun ldvPseudonimiseerder(): LdvPseudonimiseerder {
        require(pseudonimiseringZout.isNotBlank()) {
            "Configuratiefout: fbs.ldv.pseudonimisering.zout mag niet leeg zijn"
        }
        require(pseudonimiseringZout.toByteArray(Charsets.UTF_8).size >= 32) {
            "Configuratiefout: fbs.ldv.pseudonimisering.zout moet minimaal 32 bytes zijn " +
                "(huidige lengte: ${pseudonimiseringZout.toByteArray(Charsets.UTF_8).size})"
        }
        log.info("LDV BSN-pseudonimisering geconfigureerd (HMAC-SHA256)")
        return LdvPseudonimiseerder.create(pseudonimiseringZout)
    }
}
