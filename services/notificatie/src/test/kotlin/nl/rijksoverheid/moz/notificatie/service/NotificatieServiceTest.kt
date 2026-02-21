package nl.rijksoverheid.moz.notificatie.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.enterprise.inject.Instance
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.NotificatieFrequentie
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.NotificatieVerzoek
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.notificatie.client.NotificatieprofielClient
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import nl.rijksoverheid.moz.notificatie.exception.NotificatieNietGevondenException
import nl.rijksoverheid.moz.notificatie.repository.NotificatieRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID
import java.util.stream.Stream

class NotificatieServiceTest {

    private val notificatieRepository = mockk<NotificatieRepository>()
    private val emailVerzender = mockk<EmailNotificatieVerzender>()
    private val smsVerzender = mockk<SmsNotificatieVerzender>()
    private val verzenders = mockk<Instance<NotificatieVerzender>>()
    private val profielClient = mockk<NotificatieprofielClient>()
    private val ldvLogger = mockk<LdvLogger>()

    private val service = NotificatieService(
        notificatieRepository, verzenders, profielClient, ldvLogger
    )

    init {
        every { ldvLogger.logVerwerking(any()) } just Runs
        every { ldvLogger.withinVerwerking(any(), any<() -> Any>()) } answers {
            val block = secondArg<() -> Any>()
            block()
        }
        every { emailVerzender.kanaal } returns NotificatieKanaal.EMAIL
        every { smsVerzender.kanaal } returns NotificatieKanaal.SMS
    }

    @Test
    fun `maakNotificatie creates with AANGEMAAKT status`() {
        val verzoek = NotificatieVerzoek(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "999999999",
            kanaal = NotificatieKanaal.EMAIL,
            onderwerp = "Test onderwerp",
            inhoud = "Test inhoud"
        )

        every { notificatieRepository.bewaar(any<NotificatieEntity>()) } just Runs

        val notificatie = service.maakNotificatie(verzoek)

        assertEquals(OntvangerIdType.BSN, notificatie.ontvangerIdType)
        assertEquals("999999999", notificatie.ontvangerId)
        assertEquals(NotificatieKanaal.EMAIL, notificatie.kanaal)
        assertEquals(NotificatieStatusWaarde.AANGEMAAKT, notificatie.status)
    }

    @Test
    fun `haalStatus returns status when found`() {
        val notificatieId = UUID.randomUUID()
        val entity = createTestEntity(notificatieId)
        every { notificatieRepository.vindOpId(notificatieId) } returns entity

        val status = service.haalStatus(notificatieId)

        assertEquals(notificatieId, status.notificatieId)
        assertEquals(NotificatieStatusWaarde.AANGEMAAKT, status.status)
    }

    @Test
    fun `haalStatus throws exception when not found`() {
        val notificatieId = UUID.randomUUID()
        every { notificatieRepository.vindOpId(notificatieId) } returns null

        assertThrows<NotificatieNietGevondenException> {
            service.haalStatus(notificatieId)
        }
    }

    @Test
    fun `verwerkBerichtOntvangen sends email when profiel has emailNotificaties`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = true,
            smsNotificaties = false,
            emailAdres = "test@example.nl"
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel
        every { notificatieRepository.bewaar(any<NotificatieEntity>()) } just Runs
        every { verzenders.stream() } returns Stream.of(emailVerzender)
        every { emailVerzender.verzend(any(), any(), any()) } just Runs

        service.verwerkBerichtOntvangen(bericht)

        verify { emailVerzender.verzend("test@example.nl", "Test onderwerp", "Test inhoud") }
    }

    @Test
    fun `verwerkBerichtOntvangen sends sms when profiel has smsNotificaties`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = false,
            smsNotificaties = true,
            telefoonnummer = "+31612345678"
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel
        every { notificatieRepository.bewaar(any<NotificatieEntity>()) } just Runs
        every { verzenders.stream() } returns Stream.of(smsVerzender)
        every { smsVerzender.verzend(any(), any(), any()) } just Runs

        service.verwerkBerichtOntvangen(bericht)

        verify { smsVerzender.verzend("+31612345678", "Test onderwerp", "Test inhoud") }
    }

    @Test
    fun `verwerkBerichtOntvangen sends both email and sms`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = true,
            smsNotificaties = true,
            emailAdres = "test@example.nl",
            telefoonnummer = "+31612345678"
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel
        every { notificatieRepository.bewaar(any<NotificatieEntity>()) } just Runs
        every { verzenders.stream() } returns Stream.of(emailVerzender) andThen Stream.of(smsVerzender)
        every { emailVerzender.verzend(any(), any(), any()) } just Runs
        every { smsVerzender.verzend(any(), any(), any()) } just Runs

        service.verwerkBerichtOntvangen(bericht)

        verify { emailVerzender.verzend("test@example.nl", any(), any()) }
        verify { smsVerzender.verzend("+31612345678", any(), any()) }
    }

    @Test
    fun `verwerkBerichtOntvangen skips when no profiel found`() {
        val bericht = createTestBericht()
        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } throws
            RuntimeException("Service unavailable")

        service.verwerkBerichtOntvangen(bericht)

        verify(exactly = 0) { notificatieRepository.bewaar(any<NotificatieEntity>()) }
    }

    @Test
    fun `verwerkBerichtOntvangen sets MISLUKT on send failure`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = true,
            smsNotificaties = false,
            emailAdres = "test@example.nl"
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel
        every { notificatieRepository.bewaar(any<NotificatieEntity>()) } just Runs
        every { verzenders.stream() } returns Stream.of(emailVerzender)
        every { emailVerzender.verzend(any(), any(), any()) } throws RuntimeException("SMTP error")

        service.verwerkBerichtOntvangen(bericht)

        verify(exactly = 2) { notificatieRepository.bewaar(match<NotificatieEntity> { true }) }
    }

    private fun createTestEntity(id: UUID = UUID.randomUUID()) = NotificatieEntity(
        id = id,
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "999999999",
        kanaal = NotificatieKanaal.EMAIL,
        onderwerp = "Test onderwerp",
        inhoud = "Test inhoud",
        status = NotificatieStatusWaarde.AANGEMAAKT,
        aangemaaktOp = Instant.now()
    )

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
