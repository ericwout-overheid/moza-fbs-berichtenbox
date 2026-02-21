package nl.rijksoverheid.moz.notificatie.resource

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import nl.rijksoverheid.moz.common.model.NotificatieVerzoek
import nl.rijksoverheid.moz.notificatie.service.NotificatieService
import java.util.UUID

@Path("/api/v1/notificaties")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class NotificatieResource(
    private val notificatieService: NotificatieService
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun maakNotificatie(verzoek: NotificatieVerzoek): Response {
        val notificatie = notificatieService.maakNotificatie(verzoek)
        return Response.status(Response.Status.ACCEPTED).entity(notificatie).build()
    }

    @GET
    @Path("/{notificatieId}/status")
    fun haalStatus(@PathParam("notificatieId") notificatieId: UUID): Response {
        val status = notificatieService.haalStatus(notificatieId)
        return Response.ok(status).build()
    }
}
