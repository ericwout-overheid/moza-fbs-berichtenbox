package nl.rijksoverheid.moz.berichtenmagazijn.config

import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Optional

/**
 * Resolves het OIN van de afzender uit de security context.
 *
 * In productie wordt het OIN geëxtraheerd uit de JWT `client_id` claim
 * (OAuth 2.0 NL profiel) of de mTLS certificaat Subject DN (Digikoppeling).
 *
 * In dev-modus (fbs.security.dev-mode=true) valt terug op een geconfigureerd OIN.
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

        // OAuth 2.0 NL profiel: client_id claim bevat het OIN
        val oin = securityIdentity.getAttribute<String>("client_id")
            ?: securityIdentity.getAttribute<String>("azp")
            ?: principal.name

        require(oin.matches(Regex("^\\d{20}$"))) {
            "OIN uit security context is niet geldig (verwacht 20 cijfers): $oin"
        }

        return oin
    }
}
