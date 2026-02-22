package nl.rijksoverheid.moz.admindashboard.ui.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.inject.Inject
import nl.rijksoverheid.moz.admindashboard.service.DashboardDataService
import nl.rijksoverheid.moz.admindashboard.ui.component.StatusBadge
import java.util.UUID

@Route("notificaties")
@PageTitle("Notificaties - FBS Admin")
class NotificatiesView @Inject constructor(
    private val dataService: DashboardDataService
) : VerticalLayout() {

    private val resultaatLayout = VerticalLayout()

    init {
        add(H2("Notificatie Status Opzoeken"))

        val zoekLayout = HorizontalLayout()
        zoekLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.END

        val idVeld = TextField("Notificatie ID (UUID)")
        idVeld.placeholder = "bijv. 550e8400-e29b-41d4-a716-446655440000"
        idVeld.width = "400px"

        val zoekKnop = Button("Zoek") { zoekNotificatie(idVeld.value) }

        zoekLayout.add(idVeld, zoekKnop)
        add(zoekLayout)

        resultaatLayout.isVisible = false
        add(resultaatLayout)
    }

    private fun zoekNotificatie(idStr: String) {
        resultaatLayout.removeAll()
        resultaatLayout.isVisible = true

        val id = try {
            UUID.fromString(idStr.trim())
        } catch (e: IllegalArgumentException) {
            resultaatLayout.add(Span("Ongeldig UUID formaat"))
            return
        }

        val result = dataService.haalNotificatieStatus(id)
        if (result.isFout) {
            val foutSpan = Span(result.foutmelding)
            foutSpan.style.set("color", "var(--lumo-error-text-color)")
            resultaatLayout.add(foutSpan)
            return
        }

        val status = result.data
        if (status == null) {
            resultaatLayout.add(Span("Notificatie niet gevonden"))
            return
        }

        resultaatLayout.add(
            HorizontalLayout(Span("Notificatie ID:"), Span(status.notificatieId.toString())),
            HorizontalLayout(
                Span("Status:"),
                StatusBadge.voorNotificatieStatus(status.status)
            )
        )

        status.verzondenOp?.let {
            resultaatLayout.add(HorizontalLayout(Span("Verzonden op:"), Span(it.toString())))
        }
        status.afgeleverdOp?.let {
            resultaatLayout.add(HorizontalLayout(Span("Afgeleverd op:"), Span(it.toString())))
        }
        status.foutmelding?.let {
            val foutSpan = Span(it)
            foutSpan.style.set("color", "var(--lumo-error-text-color)")
            resultaatLayout.add(HorizontalLayout(Span("Foutmelding:"), foutSpan))
        }
    }
}
