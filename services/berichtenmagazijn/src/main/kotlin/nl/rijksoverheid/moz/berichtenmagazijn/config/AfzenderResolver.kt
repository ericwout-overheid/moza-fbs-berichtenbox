package nl.rijksoverheid.moz.berichtenmagazijn.config

import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Optional

/**
 * Resolvet het OIN van de afzender uit de security context.
 *
 * In productie wordt het OIN bepaald via (in volgorde):
 * 1. JWT `client_id` claim (OAuth 2.0 NL profiel)
 * 2. JWT `azp` claim (authorized party, fallback)
 * 3. Principal name uit de security context
 *
 * In dev-modus (fbs.security.dev-mode=true) wordt een geconfigureerd OIN gebruikt.
 */
@ApplicationScoped
class AfzenderResolver(
    private val securityIdentity: SecurityIdentity,
    @param:ConfigProperty(name = "fbs.security.dev-mode", defaultValue = "false")
    private val devMode: Boolean,
    @param:ConfigProperty(name = "fbs.dev.afzender-oin")
    private val devAfzenderOin: Optional<String>
) {
    private val log = Logger.getLogger(AfzenderResolver::class.java)

    /**
     * Resolvet het OIN van de huidige afzender.
     *
     * @return het 20-cijferig OIN van de afzender
     * @throws IllegalStateException als het OIN niet bepaald kan worden
     */
    fun resolve(): String {
        if (devMode) {
            val oin = devAfzenderOin.orElseThrow {
                IllegalStateException("Dev-modus actief maar fbs.dev.afzender-oin niet geconfigureerd")
            }
            log.debugf("Dev-modus: afzenderOin=%s uit configuratie", oin)
            return oin
        }

        val principal = securityIdentity.principal
            ?: throw IllegalStateException("Geen security principal beschikbaar — is authenticatie geconfigureerd?")

        val clientId = securityIdentity.getAttribute<String>("client_id")
        if (clientId != null) {
            log.debugf("OIN opgelost uit client_id claim: %s", clientId)
            return validateOin(clientId)
        }

        val azp = securityIdentity.getAttribute<String>("azp")
        if (azp != null) {
            log.warnf("client_id claim ontbreekt, fallback naar azp claim: %s", azp)
            return validateOin(azp)
        }

        log.warnf("client_id en azp claims ontbreken, fallback naar principal.name: %s", principal.name)
        return validateOin(principal.name)
    }

    private fun validateOin(oin: String): String {
        require(oin.matches(OIN_PATTERN)) {
            "OIN uit security context is niet geldig (verwacht 20 cijfers): $oin"
        }
        return oin
    }

    companion object {
        private val OIN_PATTERN = Regex("^\\d{20}$")
    }
}
