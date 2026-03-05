package nl.rijksoverheid.moz.demo.mock

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestStreamElementType
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant

data class DemoEvent(
    val type: String,
    val afzender: String? = null,
    val ontvanger: String? = null,
    val onderwerp: String? = null,
    val detail: String? = null,
    val timestamp: Instant = Instant.now()
)

@ApplicationScoped
class SseEventPublisher(
    private val objectMapper: ObjectMapper
) {
    private val processor: BroadcastProcessor<String> = BroadcastProcessor.create()
    private val recentEvents = mutableListOf<DemoEvent>()

    fun publish(event: DemoEvent) {
        synchronized(recentEvents) {
            recentEvents.add(event)
            if (recentEvents.size > 200) {
                recentEvents.removeFirst()
            }
        }
        processor.onNext(objectMapper.writeValueAsString(event))
    }

    fun stream(): Multi<String> = processor

    fun recentEvents(): List<DemoEvent> = synchronized(recentEvents) {
        recentEvents.toList()
    }
}

@Path("/api/demo/events")
class SseEventResource(
    private val publisher: SseEventPublisher
) {
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    fun stream(): Multi<String> = publisher.stream()
}
