package nl.rijksoverheid.moz.notificatie.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.enterprise.inject.Instance
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import nl.rijksoverheid.moz.notificatie.event.NotificatieEventPublisher
import nl.rijksoverheid.moz.notificatie.repository.NotificatieRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID
import java.util.stream.Stream

class NotificatieVerzendServiceTest {

    private val notificatieRepository = mockk<NotificatieRepository>()
    private val emailVerzender = mockk<EmailNotificatieVerzender>()
    private val verzenders = mockk<Instance<NotificatieVerzender>>()
    private val eventPublisher = mockk<NotificatieEventPublisher>()

    private val service = NotificatieVerzendService(
        notificatieRepository, verzenders, eventPublisher
    )

    init {
        every { emailVerzender.kanaal } returns NotificatieKanaal.EMAIL
    }

    @Test
    fun `verzend notificatie markeert als VERZONDEN bij succes`() {
        val bericht = createTestBericht()
        val entitySlot = slot<NotificatieEntity>()

        every { notificatieRepository.bewaar(capture(entitySlot)) } just Runs
        every { verzenders.stream() } returns Stream.of(emailVerzender)
        every { emailVerzender.verzend(any(), any(), any()) } just Runs
        every { eventPublisher.publishNotificatieVerzonden(any()) } just Runs

        service.verzendNotificatie(bericht, NotificatieKanaal.EMAIL, "test@example.nl")

        assertEquals(NotificatieStatusWaarde.VERZONDEN, entitySlot.captured.status)
        assertNotNull(entitySlot.captured.verzondenOp)
        verify { emailVerzender.verzend("test@example.nl", "Test onderwerp", "Test inhoud") }
        verify { eventPublisher.publishNotificatieVerzonden(any()) }
    }

    @Test
    fun `verzend notificatie markeert als MISLUKT bij verzendfout`() {
        val bericht = createTestBericht()
        val entitySlot = slot<NotificatieEntity>()

        every { notificatieRepository.bewaar(capture(entitySlot)) } just Runs
        every { verzenders.stream() } returns Stream.of(emailVerzender)
        every { emailVerzender.verzend(any(), any(), any()) } throws RuntimeException("SMTP error")

        service.verzendNotificatie(bericht, NotificatieKanaal.EMAIL, "test@example.nl")

        assertEquals(NotificatieStatusWaarde.MISLUKT, entitySlot.captured.status)
        assertEquals("SMTP error", entitySlot.captured.foutmelding)
        assertNull(entitySlot.captured.verzondenOp)
        verify(exactly = 0) { eventPublisher.publishNotificatieVerzonden(any()) }
    }

    @Test
    fun `verzend notificatie gooit door bij ontbrekende verzender`() {
        val bericht = createTestBericht()

        every { notificatieRepository.bewaar(any<NotificatieEntity>()) } just Runs
        every { verzenders.stream() } returns Stream.empty()

        assertThrows<IllegalStateException> {
            service.verzendNotificatie(bericht, NotificatieKanaal.EMAIL, "test@example.nl")
        }
    }

    private fun createTestBericht() = Bericht(
        id = UUID.randomUUID(),
        afzenderOin = "00000001234567890000",
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "999999999",
        onderwerp = "Test onderwerp",
        inhoud = "Test inhoud",
        status = BerichtStatus.NIEUW,
        aangemaaktOp = Instant.now()
    )
}
