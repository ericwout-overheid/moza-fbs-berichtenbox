package nl.rijksoverheid.moz.admindashboard.ui.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.inject.Inject
import nl.rijksoverheid.moz.admindashboard.service.DashboardDataService
import nl.rijksoverheid.moz.admindashboard.ui.component.StatusBadge
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus

@Route("berichten")
@PageTitle("Berichten - FBS Admin")
class BerichtenView @Inject constructor(
    private val dataService: DashboardDataService
) : VerticalLayout() {

    private var huidigePagina = 1
    private var huidigeStatus: BerichtStatus? = null
    private val pageSize = 20
    private val grid = Grid(Bericht::class.java, false)
    private val paginaInfo = Span()

    init {
        add(H2("Berichten"))

        val filterLayout = HorizontalLayout()
        val statusFilter = ComboBox<BerichtStatus>("Status")
        statusFilter.setItems(*BerichtStatus.entries.toTypedArray())
        statusFilter.isClearButtonVisible = true
        statusFilter.addValueChangeListener {
            huidigeStatus = it.value
            huidigePagina = 1
            laadBerichten()
        }
        filterLayout.add(statusFilter)
        add(filterLayout)

        grid.addColumn({ it.id.toString().substring(0, 8) }).setHeader("ID")
        grid.addColumn({ it.afzenderOin }).setHeader("Afzender OIN")
        grid.addColumn({ maskeerOntvanger(it.ontvangerId) }).setHeader("Ontvanger")
        grid.addColumn({ it.onderwerp }).setHeader("Onderwerp")
        grid.addComponentColumn { StatusBadge.voorBerichtStatus(it.status) }
            .setHeader("Status")
        grid.addColumn({ it.aangemaaktOp.toString() }).setHeader("Aangemaakt")
        add(grid)

        val paginatieLayout = HorizontalLayout()
        paginatieLayout.defaultVerticalComponentAlignment = com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER
        val vorigeKnop = Button("Vorige") {
            if (huidigePagina > 1) {
                huidigePagina--
                laadBerichten()
            }
        }
        val volgendeKnop = Button("Volgende") {
            huidigePagina++
            laadBerichten()
        }
        paginatieLayout.add(vorigeKnop, paginaInfo, volgendeKnop)
        add(paginatieLayout)

        laadBerichten()
    }

    private fun laadBerichten() {
        val pagina = dataService.haalBerichten(page = huidigePagina, pageSize = pageSize)
        grid.setItems(pagina.resultaten)
        paginaInfo.text = "Pagina $huidigePagina van ${pagina.totaalPaginas} (${pagina.totaalElementen} berichten)"
    }

    private fun maskeerOntvanger(id: String): String =
        if (id.length > 4) "${"*".repeat(id.length - 4)}${id.takeLast(4)}" else "****"
}
