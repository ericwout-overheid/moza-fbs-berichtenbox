package nl.rijksoverheid.moz.notificatieprofiel.resource

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import nl.rijksoverheid.moz.notificatieprofiel.service.ProfielService

@Path("/api/v1/profielen")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class ProfielResource(
    private val profielService: ProfielService
) {

    @GET
    @Path("/{ontvangerId}")
    fun haalProfiel(
        @PathParam("ontvangerId") ontvangerId: String,
        @QueryParam("ontvangerIdType") @DefaultValue("BSN") ontvangerIdType: OntvangerIdType
    ): Response {
        val profiel = profielService.haalProfiel(ontvangerId, ontvangerIdType)
        return Response.ok(profiel).build()
    }

    @PUT
    @Path("/{ontvangerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun werkProfielBij(
        @PathParam("ontvangerId") ontvangerId: String,
        @QueryParam("ontvangerIdType") @DefaultValue("BSN") ontvangerIdType: OntvangerIdType,
        profiel: Profiel
    ): Response {
        val result = profielService.werkProfielBij(ontvangerId, ontvangerIdType, profiel)
        return Response.ok(result).build()
    }
}
