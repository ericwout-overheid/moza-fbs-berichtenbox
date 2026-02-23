package nl.rijksoverheid.moz.berichtenmagazijn.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.authzen.AuthZenClient
import nl.rijksoverheid.moz.authzen.AuthZenConfiguration
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Optional

/** CDI producer voor AuthZenClient (FTV/AuthZEN autorisatie). */
@ApplicationScoped
class AuthZenProducer(
    @param:ConfigProperty(name = "fbs.authzen.pdp-url")
    private val pdpUrl: Optional<String>
) {
    private val log = Logger.getLogger(AuthZenProducer::class.java)

    @Produces
    @ApplicationScoped
    fun authZenClient(): AuthZenClient {
        val url = pdpUrl.orElse("http://localhost:8090")
        log.infof("AuthZEN PDP geconfigureerd: %s", url)
        return AuthZenClient(AuthZenConfiguration(pdpUrl = url))
    }
}
