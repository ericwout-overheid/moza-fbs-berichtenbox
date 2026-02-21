package nl.rijksoverheid.moz.notificatie.service

import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.NotificatieKanaal

@ApplicationScoped
class EmailNotificatieVerzender(
    private val mailer: Mailer
) : NotificatieVerzender {

    override val kanaal = NotificatieKanaal.EMAIL

    override fun verzend(adres: String, onderwerp: String, inhoud: String) {
        mailer.send(Mail.withText(adres, onderwerp, inhoud))
    }
}
