package nl.rijksoverheid.moz.berichtenlijst.client

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Page
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/api/v1/berichten")
@RegisterRestClient(configKey = "berichtenmagazijn-api")
interface BerichtenmagazijnClient {

    @GET
    fun lijstBerichten(
        @QueryParam("ontvangerIdType") ontvangerIdType: OntvangerIdType,
        @QueryParam("ontvangerId") ontvangerId: String,
        @QueryParam("page") page: Int,
        @QueryParam("pageSize") pageSize: Int
    ): Page<Bericht>
}
