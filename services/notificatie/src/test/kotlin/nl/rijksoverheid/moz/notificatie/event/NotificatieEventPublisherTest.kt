package nl.rijksoverheid.moz.notificatie.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.cloudevents.CloudEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.Notificatie
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class NotificatieEventPublisherTest {

    private val emitter = mockk<Emitter<CloudEvent>>()
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    private val publisher = NotificatieEventPublisher(emitter, objectMapper)

    @Test
    fun `publishNotificatieVerzonden verstuurt CloudEvent via emitter`() {
        val notificatie = createTestNotificatie()
        every { emitter.send(any<CloudEvent>()) } returns CompletableFuture.completedFuture(null)

        publisher.publishNotificatieVerzonden(notificatie)

        verify { emitter.send(any<CloudEvent>()) }
    }

    @Test
    fun `publishNotificatieVerzonden vangt exception op zonder te gooien`() {
        val notificatie = createTestNotificatie()
        every { emitter.send(any<CloudEvent>()) } throws RuntimeException("Kafka unavailable")

        assertDoesNotThrow {
            publisher.publishNotificatieVerzonden(notificatie)
        }
    }

    private fun createTestNotificatie() = Notificatie(
        id = UUID.randomUUID(),
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "999999999",
        kanaal = NotificatieKanaal.EMAIL,
        onderwerp = "Test onderwerp",
        inhoud = "Test inhoud",
        status = NotificatieStatusWaarde.VERZONDEN,
        aangemaaktOp = Instant.now(),
        verzondenOp = Instant.now()
    )
}
