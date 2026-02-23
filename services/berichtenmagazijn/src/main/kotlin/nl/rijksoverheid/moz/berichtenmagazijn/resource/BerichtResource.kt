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
import nl.rijksoverheid.moz.berichtenmagazijn.event.BerichtEventPublisher
import nl.rijksoverheid.moz.berichtenmagazijn.service.BerichtService
import nl.rijksoverheid.moz.common.model.BerichtAanmaakVerzoek
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.BerichtStatusWijziging
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.eclipse.microprofile.config.inject.ConfigProperty
import jakarta.ws.rs.core.Context
import java.net.URI
import java.util.UUID

@Path("/api/v1/berichten")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class BerichtResource(
    private val berichtService: BerichtService,
    private val eventPublisher: BerichtEventPublisher,
    @param:ConfigProperty(name = "fbs.dev.afzender-oin") private val afzenderOin: String
) {
    @Context
    lateinit var uriInfo: UriInfo

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun maakBericht(verzoek: BerichtAanmaakVerzoek): Response {
        // SECURITY: In productie moet afzenderOin uit de OIDC/mTLS security context komen.
        val bericht = berichtService.maakBericht(afzenderOin, verzoek)
        eventPublisher.publishBerichtOntvangen(afzenderOin, bericht)
        val location = uriInfo.absolutePathBuilder.path(bericht.id.toString()).build()
        return Response.created(location).entity(bericht).build()
    }

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
        // SECURITY: autorisatie vereist (FTV/AuthZEN) — nog niet geïmplementeerd.
        val bericht = berichtService.werkBerichtBij(berichtId, wijziging)
        if (wijziging.status == BerichtStatus.GELEZEN) {
            eventPublisher.publishBerichtGelezen(bericht.afzenderOin, bericht)
        }
        return Response.ok(bericht).build()
    }

    @DELETE
    @Path("/{berichtId}")
    fun verwijderBericht(@PathParam("berichtId") berichtId: UUID): Response {
        // SECURITY: autorisatie vereist (FTV/AuthZEN) — nog niet geïmplementeerd.
        val afzenderOin = berichtService.verwijderBericht(berichtId)
        eventPublisher.publishBerichtVerwijderd(afzenderOin, berichtId)
        return Response.noContent().build()
    }

}
