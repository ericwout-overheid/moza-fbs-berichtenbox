package nl.rijksoverheid.moz.admindashboard.ui.component

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span

/**
 * Kaart-component met label en waarde voor dashboard-overzicht.
 */
class StatCard(label: String, waarde: String) : Div() {

    init {
        addClassName("stat-card")
        style.set("border", "1px solid var(--lumo-contrast-20pct)")
        style.set("border-radius", "var(--lumo-border-radius-m)")
        style.set("padding", "var(--lumo-space-m)")
        style.set("text-align", "center")

        val waardeLabel = H3(waarde)
        waardeLabel.style.set("margin", "0")

        val beschrijving = Span(label)
        beschrijving.style.set("color", "var(--lumo-secondary-text-color)")
        beschrijving.style.set("font-size", "var(--lumo-font-size-s)")

        add(waardeLabel, beschrijving)
    }
}
