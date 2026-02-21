package nl.rijksoverheid.moz.notificatie.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import org.jboss.logging.Logger

@ApplicationScoped
class SmsNotificatieVerzender : NotificatieVerzender {

    private val log = Logger.getLogger(SmsNotificatieVerzender::class.java)

    override val kanaal = NotificatieKanaal.SMS

    override fun verzend(adres: String, onderwerp: String, inhoud: String) {
        log.infof("SMS notificatie (mock): naar=%s, onderwerp=%s", adres, onderwerp)
    }
}
