package nl.rijksoverheid.moz.berichtenlijst.resource

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import nl.rijksoverheid.moz.berichtenlijst.service.BerichtenlijstService
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.common.model.OntvangerIdType

@Path("/api/v1/berichtenlijst")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class BerichtenlijstResource(
    private val berichtenlijstService: BerichtenlijstService
) {

    @GET
    fun haalBerichtenlijst(
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType,
        @QueryParam("ontvangerId") ontvangerId: String,
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?
    ): Response {
        val result = berichtenlijstService.haalBerichtenlijst(
            ontvangerIdType = ontvangerIdType,
            ontvangerId = ontvangerId,
            page = page ?: 1,
            pageSize = pageSize ?: FbsConstants.DEFAULT_PAGE_SIZE
        )
        return Response.ok(result).build()
    }

    @GET
    @Path("/zoek")
    fun zoekBerichten(
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType,
        @QueryParam("ontvangerId") ontvangerId: String,
        @QueryParam("zoekterm") zoekterm: String,
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?
    ): Response {
        val result = berichtenlijstService.zoekBerichten(
            ontvangerIdType = ontvangerIdType,
            ontvangerId = ontvangerId,
            zoekterm = zoekterm,
            page = page ?: 1,
            pageSize = pageSize ?: FbsConstants.DEFAULT_PAGE_SIZE
        )
        return Response.ok(result).build()
    }
}
