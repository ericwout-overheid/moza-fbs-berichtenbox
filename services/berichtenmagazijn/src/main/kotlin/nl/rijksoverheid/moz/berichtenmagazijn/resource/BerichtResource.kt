package nl.rijksoverheid.moz.berichtenmagazijn.resource

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import nl.rijksoverheid.moz.berichtenmagazijn.config.AfzenderResolver
import nl.rijksoverheid.moz.berichtenmagazijn.event.BerichtEventPublisher
import nl.rijksoverheid.moz.berichtenmagazijn.service.AutorisatieService
import nl.rijksoverheid.moz.berichtenmagazijn.service.BerichtService
import nl.rijksoverheid.moz.common.model.BerichtAanmaakVerzoek
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.BerichtStatusWijziging
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import jakarta.ws.rs.core.Context
import java.util.UUID

@Path("/api/v1/berichten")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class BerichtResource(
    private val berichtService: BerichtService,
    private val eventPublisher: BerichtEventPublisher,
    private val afzenderResolver: AfzenderResolver,
    private val autorisatieService: AutorisatieService
) {
    @Context
    lateinit var uriInfo: UriInfo

    // Authenticatie is voldoende voor aanmaken — AuthZEN autorisatie per-resource
    // is niet mogelijk omdat het bericht-ID nog niet bestaat.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun maakBericht(verzoek: BerichtAanmaakVerzoek): Response {
        val afzenderOin = afzenderResolver.resolve()
        val bericht = berichtService.maakBericht(afzenderOin, verzoek)
        eventPublisher.publishBerichtOntvangen(afzenderOin, bericht)
        val location = uriInfo.absolutePathBuilder.path(bericht.id.toString()).build()
        return Response.created(location).entity(bericht).build()
    }

    // Lijst-endpoint filtert op ontvanger; per-resource AuthZEN is niet toepasbaar.
    @GET
    fun lijstBerichten(
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType?,
        @QueryParam("ontvangerId") ontvangerId: String?,
        @QueryParam("status") status: BerichtStatus?,
        @QueryParam("onderwerp") onderwerp: String?,
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("pageSize") @DefaultValue("20") pageSize: Int
    ): Response {
        val pagina = berichtService.lijstBerichten(ontvangerIdType, ontvangerId, status, onderwerp, page, pageSize)
        return Response.ok(pagina).build()
    }

    @GET
    @Path("/{berichtId}")
    fun haalBericht(@PathParam("berichtId") berichtId: UUID): Response {
        val afzenderOin = afzenderResolver.resolve()
        autorisatieService.controleerToegang(afzenderOin, "read", berichtId)
        val bericht = berichtService.haalBericht(berichtId)
        return Response.ok(bericht).build()
    }

    @PATCH
    @Path("/{berichtId}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun werkBerichtBij(
        @PathParam("berichtId") berichtId: UUID,
        wijziging: BerichtStatusWijziging
    ): Response {
        val afzenderOin = afzenderResolver.resolve()
        autorisatieService.controleerToegang(afzenderOin, "update", berichtId)
        val bericht = berichtService.werkBerichtBij(berichtId, wijziging)
        if (wijziging.status == BerichtStatus.GELEZEN) {
            eventPublisher.publishBerichtGelezen(bericht.afzenderOin, bericht)
        }
        return Response.ok(bericht).build()
    }

    @DELETE
    @Path("/{berichtId}")
    fun verwijderBericht(@PathParam("berichtId") berichtId: UUID): Response {
        val afzenderOin = afzenderResolver.resolve()
        autorisatieService.controleerToegang(afzenderOin, "delete", berichtId)
        val afzenderOinBericht = berichtService.verwijderBericht(berichtId)
        eventPublisher.publishBerichtVerwijderd(afzenderOinBericht, berichtId)
        return Response.noContent().build()
    }
}
