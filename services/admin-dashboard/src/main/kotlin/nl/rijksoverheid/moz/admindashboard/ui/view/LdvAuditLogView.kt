package nl.rijksoverheid.moz.admindashboard.ui.view

import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.eclipse.microprofile.config.inject.ConfigProperty

@Route("ldv-audit-log")
@PageTitle("LDV Audit Log - FBS Admin")
class LdvAuditLogView(
    @param:ConfigProperty(name = "fbs.jaeger.ui.url")
    private val jaegerUrl: String
) : VerticalLayout() {

    init {
        add(H2("LDV Audit Log"))

        add(Paragraph(
            "Het Logboek Dataverwerkingen (LDV) registreert alle dataverwerkingen " +
                "als OpenTelemetry traces. Deze traces zijn te raadplegen via de Jaeger UI."
        ))

        val link = Anchor(jaegerUrl, "Open Jaeger UI")
        link.setTarget("_blank")
        add(link)

        add(Paragraph("Zoektips:"))
        val tips = listOf(
            "Zoek op service: selecteer de gewenste service in het Service-dropdown",
            "Zoek op operatie: gebruik het Operation-veld om specifieke API-calls te vinden",
            "Zoek op tijdsperiode: pas de Lookback-periode aan voor historische verwerkingen",
            "Zoek op tag: gebruik 'fbs.bericht.id=<uuid>' om traces voor een specifiek bericht te vinden"
        )
        val tipsList = com.vaadin.flow.component.html.UnorderedList()
        tips.forEach { tipsList.add(com.vaadin.flow.component.html.ListItem(it)) }
        add(tipsList)
    }
}
