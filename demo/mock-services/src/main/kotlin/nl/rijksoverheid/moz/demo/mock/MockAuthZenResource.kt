package nl.rijksoverheid.moz.demo.mock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.jboss.logging.Logger

@Path("/access/v1")
@ApplicationScoped
class MockAuthZenResource(
    private val ssePublisher: SseEventPublisher,
    private val objectMapper: ObjectMapper
) {
    private val log = Logger.getLogger(MockAuthZenResource::class.java)

    @POST
    @Path("/evaluation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun evaluate(
        body: JsonNode,
        @QueryParam("deny") deny: Boolean?
    ): Map<String, Any> {
        val subject = body.path("subject").path("id").asText("onbekend")
        val action = body.path("action").path("name").asText("onbekend")
        val resource = body.path("resource").path("type").asText("onbekend")
        val decision = deny != true

        log.infof("AuthZEN evaluatie: subject=%s, action=%s, resource=%s, decision=%s",
            subject, action, resource, decision)

        ssePublisher.publish(
            DemoEvent(
                type = "AUTHZEN_EVALUATIE",
                detail = "Subject=$subject action=$action resource=$resource → ${if (decision) "PERMIT" else "DENY"}"
            )
        )

        return mapOf("decision" to decision)
    }
}
