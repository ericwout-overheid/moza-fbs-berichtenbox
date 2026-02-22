package nl.rijksoverheid.moz.admindashboard.ui.component

import com.vaadin.flow.component.html.Span
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde

/**
 * Kleur-gecodeerd badge component voor status weergave.
 */
class StatusBadge(tekst: String, kleur: BadgeKleur) : Span(tekst) {

    init {
        element.themeList.add("badge")
        when (kleur) {
            BadgeKleur.GROEN -> element.themeList.add("success")
            BadgeKleur.ROOD -> element.themeList.add("error")
            BadgeKleur.BLAUW -> element.themeList.add("primary")
            BadgeKleur.GRIJS -> element.themeList.add("contrast")
        }
    }

    enum class BadgeKleur { GROEN, ROOD, BLAUW, GRIJS }

    companion object {
        fun voorBerichtStatus(status: BerichtStatus) = StatusBadge(
            tekst = status.name,
            kleur = when (status) {
                BerichtStatus.NIEUW -> BadgeKleur.GRIJS
                BerichtStatus.GELEZEN -> BadgeKleur.BLAUW
                BerichtStatus.GEARCHIVEERD -> BadgeKleur.GROEN
            }
        )

        fun voorNotificatieStatus(status: NotificatieStatusWaarde) = StatusBadge(
            tekst = status.name,
            kleur = when (status) {
                NotificatieStatusWaarde.AANGEMAAKT -> BadgeKleur.GRIJS
                NotificatieStatusWaarde.VERZONDEN -> BadgeKleur.BLAUW
                NotificatieStatusWaarde.AFGELEVERD -> BadgeKleur.GROEN
                NotificatieStatusWaarde.MISLUKT -> BadgeKleur.ROOD
                NotificatieStatusWaarde.DEFINITIEF_MISLUKT -> BadgeKleur.ROOD
            }
        )

        fun voorBeschikbaarheid(beschikbaar: Boolean) = StatusBadge(
            tekst = if (beschikbaar) "UP" else "DOWN",
            kleur = if (beschikbaar) BadgeKleur.GROEN else BadgeKleur.ROOD
        )
    }
}
