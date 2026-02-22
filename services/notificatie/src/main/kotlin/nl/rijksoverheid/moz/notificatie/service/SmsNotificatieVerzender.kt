package nl.rijksoverheid.moz.notificatie.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@ApplicationScoped
class SmsNotificatieVerzender(
    @param:ConfigProperty(name = "fbs.notificatie.sms.stub-modus", defaultValue = "false")
    private val stubModus: Boolean
) : NotificatieVerzender {

    private val log = Logger.getLogger(SmsNotificatieVerzender::class.java)

    override val kanaal = NotificatieKanaal.SMS

    // TODO: Implementeer echte SMS-gateway integratie (bijv. MessageBird, CM.com)
    override fun verzend(adres: String, onderwerp: String, inhoud: String) {
        if (stubModus) {
            log.warnf("SMS notificatie (stub): naar=%s, onderwerp=%s — geen echte SMS verzonden", adres, onderwerp)
            return
        }
        throw UnsupportedOperationException(
            "SMS-gateway is niet geconfigureerd. Zet fbs.notificatie.sms.stub-modus=true voor ontwikkeling."
        )
    }
}
