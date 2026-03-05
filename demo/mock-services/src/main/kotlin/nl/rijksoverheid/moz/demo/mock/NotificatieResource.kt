package nl.rijksoverheid.moz.demo.mock

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/api/demo/notificaties")
class NotificatieResource(
    private val consumer: MockNotificatieConsumer
) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun lijstNotificaties(): List<DemoEvent> = consumer.recenteNotificaties()
}
