package nl.rijksoverheid.moz.admindashboard.ui.view

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.inject.Inject
import nl.rijksoverheid.moz.admindashboard.service.DashboardDataService
import nl.rijksoverheid.moz.admindashboard.service.ServiceHealthChecker
import nl.rijksoverheid.moz.admindashboard.service.ServiceStatus
import nl.rijksoverheid.moz.admindashboard.ui.component.StatCard
import nl.rijksoverheid.moz.admindashboard.ui.component.StatusBadge
import nl.rijksoverheid.moz.common.model.Bericht

@Route("")
@PageTitle("Dashboard - FBS Admin")
class DashboardView @Inject constructor(
    private val dataService: DashboardDataService,
    private val healthChecker: ServiceHealthChecker
) : VerticalLayout() {

    init {
        add(H2("Dashboard"))
        laadOverzicht()
    }

    private fun laadOverzicht() {
        val berichtenResult = dataService.haalBerichten(page = 1, pageSize = 5)
        val services = healthChecker.checkAll()

        if (berichtenResult.isFout) {
            Notification.show(berichtenResult.foutmelding, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }

        val berichten = berichtenResult.data

        // Statistieken kaarten
        val statsLayout = FlexLayout()
        statsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP)
        statsLayout.style.set("gap", "var(--lumo-space-m)")

        statsLayout.add(StatCard("Totaal berichten", berichten.totaalElementen.toString()))

        val servicesUp = services.count { it.beschikbaar }
        statsLayout.add(StatCard("Services UP", "$servicesUp / ${services.size}"))

        add(statsLayout)

        // Service status overzicht
        add(H3("Service Status"))
        val serviceGrid = Grid(ServiceStatus::class.java, false)
        serviceGrid.addColumn({ it.naam }).setHeader("Service")
        serviceGrid.addComponentColumn { StatusBadge.voorBeschikbaarheid(it.beschikbaar) }
            .setHeader("Status")
        serviceGrid.addColumn({ it.responseTimeMs }).setHeader("Response (ms)")
        serviceGrid.setItems(services)
        serviceGrid.height = "auto"
        add(serviceGrid)

        // Recente berichten
        add(H3("Recente berichten"))
        val berichtenGrid = Grid(Bericht::class.java, false)
        berichtenGrid.addColumn({ it.id.toString().substring(0, 8) }).setHeader("ID")
        berichtenGrid.addColumn({ it.afzenderOin }).setHeader("Afzender OIN")
        berichtenGrid.addColumn({ it.onderwerp }).setHeader("Onderwerp")
        berichtenGrid.addComponentColumn { StatusBadge.voorBerichtStatus(it.status) }
            .setHeader("Status")
        berichtenGrid.addColumn({ it.aangemaaktOp.toString() }).setHeader("Aangemaakt")
        berichtenGrid.setItems(berichten.resultaten)
        berichtenGrid.height = "auto"
        add(berichtenGrid)
    }
}
