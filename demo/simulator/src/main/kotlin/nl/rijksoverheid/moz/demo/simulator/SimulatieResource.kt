package nl.rijksoverheid.moz.demo.simulator

import io.smallrye.mutiny.Multi
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestStreamElementType

@Path("/api/demo/simulatie")
class SimulatieResource(
    private val simulatieService: SimulatieService
) {
    @POST
    @Path("/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun start(config: SimulatieConfig): SimulatieStatus =
        simulatieService.start(config)

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    fun stop(): SimulatieStatus =
        simulatieService.stop()

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    fun status(): SimulatieStatus =
        simulatieService.status()

    @GET
    @Path("/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    fun events(): Multi<String> =
        simulatieService.eventStream()
}
