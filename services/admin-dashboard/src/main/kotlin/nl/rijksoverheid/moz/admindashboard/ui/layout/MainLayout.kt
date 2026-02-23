package nl.rijksoverheid.moz.admindashboard.ui.layout

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.Layout
import nl.rijksoverheid.moz.admindashboard.ui.view.*

@Layout
class MainLayout : AppLayout() {

    init {
        val titel = H1("FBS Admin Dashboard")
        titel.style.set("font-size", "var(--lumo-font-size-l)")
        titel.style.set("margin", "0")

        addToNavbar(DrawerToggle(), titel)

        val nav = SideNav()
        nav.addItem(SideNavItem("Dashboard", DashboardView::class.java))
        nav.addItem(SideNavItem("Berichten", BerichtenView::class.java))
        nav.addItem(SideNavItem("Systeemstatus", SysteemStatusView::class.java))
        nav.addItem(SideNavItem("LDV Audit Log", LdvAuditLogView::class.java))

        addToDrawer(nav)
    }
}
