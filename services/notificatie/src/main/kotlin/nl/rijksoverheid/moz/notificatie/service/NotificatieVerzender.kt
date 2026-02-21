package nl.rijksoverheid.moz.notificatie.service

import nl.rijksoverheid.moz.common.model.NotificatieKanaal

interface NotificatieVerzender {

    val kanaal: NotificatieKanaal

    fun verzend(adres: String, onderwerp: String, inhoud: String)
}
