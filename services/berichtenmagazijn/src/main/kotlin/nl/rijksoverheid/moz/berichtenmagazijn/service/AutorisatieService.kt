package nl.rijksoverheid.moz.berichtenmagazijn.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.authzen.AuthZenClient
import nl.rijksoverheid.moz.authzen.AuthZenException
import nl.rijksoverheid.moz.authzen.model.Action
import nl.rijksoverheid.moz.authzen.model.EvaluationRequest
import nl.rijksoverheid.moz.authzen.model.Resource
import nl.rijksoverheid.moz.authzen.model.Subject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.UUID

/**
 * Autorisatieservice die AuthZEN/FTV-beslissingen afdwingt.
 *
 * Evalueert autorisatieverzoeken bij de PDP (Policy Decision Point) conform
 * het AuthZEN NL GOV profiel. In dev-modus worden checks overgeslagen.
 *
 * Bij PDP-onbereikbaarheid geldt fail-closed: toegang wordt geweigerd (503).
 */
@ApplicationScoped
class AutorisatieService(
    private val authZenClient: AuthZenClient,
    @param:ConfigProperty(name = "fbs.security.dev-mode", defaultValue = "false")
    private val devMode: Boolean
) {
    private val log = Logger.getLogger(AutorisatieService::class.java)

    /**
     * Controleert of de afzender de opgegeven actie mag uitvoeren op een bericht.
     *
     * @param afzenderOin OIN van de aanvrager
     * @param actie de gewenste actie ("read", "update", "delete")
     * @param berichtId ID van het bericht
     * @throws jakarta.ws.rs.ForbiddenException als de actie niet is toegestaan
     * @throws jakarta.ws.rs.ServiceUnavailableException als de PDP onbereikbaar is
     */
    fun controleerToegang(afzenderOin: String, actie: String, berichtId: UUID) {
        if (devMode) {
            log.debugf("Dev-modus: autorisatiecheck overgeslagen voor %s op bericht %s", actie, berichtId)
            return
        }

        val request = EvaluationRequest(
            subject = Subject(type = "organisatie", id = afzenderOin),
            action = Action(name = actie),
            resource = Resource(type = "bericht", id = berichtId.toString())
        )

        val response = try {
            authZenClient.evaluate(request)
        } catch (e: AuthZenException) {
            log.errorf(e, "AuthZEN PDP onbereikbaar voor %s op bericht %s — fail-closed", actie, berichtId)
            throw jakarta.ws.rs.ServiceUnavailableException(
                "Autorisatieservice tijdelijk niet beschikbaar, probeer het later opnieuw"
            )
        }

        if (!response.decision) {
            log.warnf("AuthZEN: toegang geweigerd voor actie=%s, bericht=%s", actie, berichtId)
            throw jakarta.ws.rs.ForbiddenException("Niet geautoriseerd voor deze actie")
        }
    }
}
