package nl.rijksoverheid.moz.digitalebereikbaarheid.resource

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import nl.rijksoverheid.moz.common.model.Bereikbaarheid
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.digitalebereikbaarheid.service.BereikbaarheidService

@Path("/api/v1/bereikbaarheid")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class BereikbaarheidResource(
    private val bereikbaarheidService: BereikbaarheidService
) {

    @GET
    @Path("/{ontvangerId}")
    fun haalBereikbaarheid(
        @PathParam("ontvangerId") ontvangerId: String,
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType
    ): Response {
        val bereikbaarheid = bereikbaarheidService.haalBereikbaarheid(ontvangerId, ontvangerIdType)
        return Response.ok(bereikbaarheid).build()
    }

    @PUT
    @Path("/{ontvangerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun registreerBereikbaarheid(
        @PathParam("ontvangerId") ontvangerId: String,
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType,
        bereikbaarheid: Bereikbaarheid
    ): Response {
        val result = bereikbaarheidService.registreerBereikbaarheid(ontvangerId, ontvangerIdType, bereikbaarheid)
        return Response.ok(result).build()
    }
}
