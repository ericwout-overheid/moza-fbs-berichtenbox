package nl.rijksoverheid.moz.berichtenmagazijn.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.authzen.AuthZenClient
import nl.rijksoverheid.moz.authzen.AuthZenConfiguration
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

/**
 * CDI producer voor AuthZenClient (FTV/AuthZEN autorisatie).
 *
 * Vereist `fbs.authzen.pdp-url` configuratie. In `application.properties` staat een
 * dev-default; in productie moet deze via omgevingsvariabele worden ingesteld.
 */
@ApplicationScoped
class AuthZenProducer(
    @param:ConfigProperty(name = "fbs.authzen.pdp-url")
    private val pdpUrl: String
) {
    private val log = Logger.getLogger(AuthZenProducer::class.java)

    @Produces
    @ApplicationScoped
    fun authZenClient(): AuthZenClient {
        log.infof("AuthZEN PDP geconfigureerd: %s", pdpUrl)
        return AuthZenClient(AuthZenConfiguration(pdpUrl = pdpUrl))
    }
}
