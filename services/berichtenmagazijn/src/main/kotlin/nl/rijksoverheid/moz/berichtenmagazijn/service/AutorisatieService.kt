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
            log.errorf(e, "AuthZEN PDP onbereikbaar voor %s op bericht %s — toegang geweigerd", actie, berichtId)
            throw jakarta.ws.rs.ForbiddenException("Autorisatieservice niet beschikbaar")
        }

        if (!response.decision) {
            log.warnf("AuthZEN: toegang geweigerd voor OIN %s, actie=%s, bericht=%s", afzenderOin, actie, berichtId)
            throw jakarta.ws.rs.ForbiddenException("Niet geautoriseerd voor deze actie")
        }
    }
}
