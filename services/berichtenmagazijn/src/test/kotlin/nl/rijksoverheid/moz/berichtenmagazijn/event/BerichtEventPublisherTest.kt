package nl.rijksoverheid.moz.berichtenmagazijn.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

class BerichtEventPublisherTest {

    private val ontvangenEmitter = mockk<Emitter<CloudEvent>>()
    private val gelezenEmitter = mockk<Emitter<CloudEvent>>()
    private val verwijderdEmitter = mockk<Emitter<CloudEvent>>()
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    private val publisher = BerichtEventPublisher(
        ontvangenEmitter, gelezenEmitter, verwijderdEmitter, objectMapper
    )

    private val testOin = "00000001234567890000"
    private val testBericht = Bericht(
        id = UUID.randomUUID(),
        afzenderOin = testOin,
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "999999999",
        onderwerp = "Test",
        inhoud = "Inhoud",
        status = BerichtStatus.NIEUW,
        aangemaaktOp = Instant.now()
    )

    @Test
    fun `publishBerichtOntvangen sends event to correct emitter`() {
        every { ontvangenEmitter.send(any<CloudEvent>()) } returns CompletableFuture.completedFuture(null)
        publisher.publishBerichtOntvangen(testOin, testBericht)
        verify(exactly = 1) { ontvangenEmitter.send(any<CloudEvent>()) }
        verify(exactly = 0) { gelezenEmitter.send(any<CloudEvent>()) }
    }

    @Test
    fun `publishBerichtGelezen sends event to correct emitter`() {
        every { gelezenEmitter.send(any<CloudEvent>()) } returns CompletableFuture.completedFuture(null)
        publisher.publishBerichtGelezen(testOin, testBericht)
        verify(exactly = 1) { gelezenEmitter.send(any<CloudEvent>()) }
    }

    @Test
    fun `publishBerichtVerwijderd sends event to correct emitter`() {
        every { verwijderdEmitter.send(any<CloudEvent>()) } returns CompletableFuture.completedFuture(null)
        publisher.publishBerichtVerwijderd(testOin, testBericht.id)
        verify(exactly = 1) { verwijderdEmitter.send(any<CloudEvent>()) }
    }

    @Test
    fun `publishBerichtVerwijderd event has no data payload`() {
        val eventSlot = slot<CloudEvent>()
        every { verwijderdEmitter.send(capture(eventSlot)) } returns CompletableFuture.completedFuture(null)
        publisher.publishBerichtVerwijderd(testOin, testBericht.id)
        assertNull(eventSlot.captured.data)
    }

    @Test
    fun `publishBerichtOntvangen event includes data payload`() {
        val eventSlot = slot<CloudEvent>()
        every { ontvangenEmitter.send(capture(eventSlot)) } returns CompletableFuture.completedFuture(null)
        publisher.publishBerichtOntvangen(testOin, testBericht)
        assertEquals("application/json", eventSlot.captured.dataContentType)
    }

    @Test
    fun `fire-and-forget - errors do not propagate`() {
        every { ontvangenEmitter.send(any<CloudEvent>()) } throws RuntimeException("Kafka down")
        assertDoesNotThrow {
            publisher.publishBerichtOntvangen(testOin, testBericht)
        }
    }

    @Test
    fun `fire-and-forget - async errors are handled`() {
        val future = CompletableFuture<Void>()
        every { ontvangenEmitter.send(any<CloudEvent>()) } returns future
        publisher.publishBerichtOntvangen(testOin, testBericht)
        assertDoesNotThrow {
            future.completeExceptionally(RuntimeException("Kafka delivery failed"))
        }
    }
}
