package nl.rijksoverheid.moz.notificatie.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import org.jboss.logging.Logger

@ApplicationScoped
class SmsNotificatieVerzender : NotificatieVerzender {

    private val log = Logger.getLogger(SmsNotificatieVerzender::class.java)

    override val kanaal = NotificatieKanaal.SMS

    // TODO: Implementeer echte SMS-gateway integratie (bijv. MessageBird, CM.com)
    override fun verzend(adres: String, onderwerp: String, inhoud: String) {
        log.warnf("SMS notificatie (stub): naar=%s, onderwerp=%s — geen echte SMS verzonden", adres, onderwerp)
    }
}
