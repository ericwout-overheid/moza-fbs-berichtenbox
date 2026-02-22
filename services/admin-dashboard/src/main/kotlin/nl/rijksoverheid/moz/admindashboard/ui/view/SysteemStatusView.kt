package nl.rijksoverheid.moz.admindashboard.ui.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.inject.Inject
import nl.rijksoverheid.moz.admindashboard.service.ServiceHealthChecker
import nl.rijksoverheid.moz.admindashboard.service.ServiceStatus
import nl.rijksoverheid.moz.admindashboard.ui.component.StatusBadge
import org.jboss.logging.Logger

@Route("systeemstatus")
@PageTitle("Systeemstatus - FBS Admin")
class SysteemStatusView @Inject constructor(
    private val healthChecker: ServiceHealthChecker
) : VerticalLayout() {

    private val log = Logger.getLogger(SysteemStatusView::class.java)
    private val grid = Grid(ServiceStatus::class.java, false)

    init {
        add(H2("Systeemstatus"))

        val verversKnop = Button("Ververs") { laadStatus() }
        add(verversKnop)

        grid.addColumn({ it.naam }).setHeader("Service")
        grid.addColumn({ it.url }).setHeader("URL")
        grid.addComponentColumn { StatusBadge.voorBeschikbaarheid(it.beschikbaar) }
            .setHeader("Status")
        grid.addColumn({ it.statusCode?.toString() ?: "-" }).setHeader("HTTP Status")
        grid.addColumn({ it.responseTimeMs?.toString() ?: "-" }).setHeader("Response (ms)")
        grid.addColumn({ it.foutmelding ?: "-" }).setHeader("Foutmelding")
        add(grid)

        laadStatus()
    }

    private fun laadStatus() {
        val statuses = try {
            healthChecker.checkAll()
        } catch (e: Exception) {
            log.errorf(e, "Health checks konden niet worden uitgevoerd")
            Notification.show("Systeemstatus kon niet worden opgehaald", 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            emptyList()
        }
        grid.setItems(statuses)
    }
}
