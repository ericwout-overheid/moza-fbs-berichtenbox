package nl.rijksoverheid.moz.notificatie.client

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/api/v1/profielen")
@RegisterRestClient(configKey = "notificatieprofiel-api")
interface NotificatieprofielClient {

    @GET
    @Path("/{ontvangerId}")
    fun haalProfiel(
        @PathParam("ontvangerId") ontvangerId: String,
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType
    ): Profiel
}
