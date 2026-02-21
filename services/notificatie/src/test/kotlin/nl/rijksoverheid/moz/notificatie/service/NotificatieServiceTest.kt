package nl.rijksoverheid.moz.notificatie.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.WebApplicationException
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
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

class NotificatieServiceTest {

    private val notificatieRepository = mockk<NotificatieRepository>()
    private val profielClient = mockk<NotificatieprofielClient>()
    private val ldvLogger = mockk<LdvLogger>()
    private val verzendService = mockk<NotificatieVerzendService>()

    private val service = NotificatieService(
        notificatieRepository, profielClient, ldvLogger, verzendService
    )

    init {
        every { ldvLogger.logVerwerking(any()) } just Runs
        every { ldvLogger.withinVerwerking(any(), any<() -> Any>()) } answers {
            val block = secondArg<() -> Any>()
            block()
        }
    }

    @Test
    fun `maakNotificatie maakt notificatie aan met status AANGEMAAKT`() {
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
    fun `haalStatus geeft status terug wanneer gevonden`() {
        val notificatieId = UUID.randomUUID()
        val entity = createTestEntity(notificatieId)
        every { notificatieRepository.vindOpId(notificatieId) } returns entity

        val status = service.haalStatus(notificatieId)

        assertEquals(notificatieId, status.notificatieId)
        assertEquals(NotificatieStatusWaarde.AANGEMAAKT, status.status)
    }

    @Test
    fun `haalStatus gooit exception wanneer niet gevonden`() {
        val notificatieId = UUID.randomUUID()
        every { notificatieRepository.vindOpId(notificatieId) } returns null

        assertThrows<NotificatieNietGevondenException> {
            service.haalStatus(notificatieId)
        }
    }

    @Test
    fun `verwerkBerichtOntvangen verzendt email wanneer profiel emailNotificaties heeft`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = true,
            smsNotificaties = false,
            emailAdres = "test@example.nl"
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel
        every { verzendService.verzendNotificatie(any(), any(), any()) } just Runs

        service.verwerkBerichtOntvangen(bericht)

        verify { verzendService.verzendNotificatie(bericht, NotificatieKanaal.EMAIL, "test@example.nl") }
    }

    @Test
    fun `verwerkBerichtOntvangen verzendt sms wanneer profiel smsNotificaties heeft`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = false,
            smsNotificaties = true,
            telefoonnummer = "+31612345678"
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel
        every { verzendService.verzendNotificatie(any(), any(), any()) } just Runs

        service.verwerkBerichtOntvangen(bericht)

        verify { verzendService.verzendNotificatie(bericht, NotificatieKanaal.SMS, "+31612345678") }
    }

    @Test
    fun `verwerkBerichtOntvangen verzendt email en sms bij beide kanalen`() {
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
        every { verzendService.verzendNotificatie(any(), any(), any()) } just Runs

        service.verwerkBerichtOntvangen(bericht)

        verify { verzendService.verzendNotificatie(bericht, NotificatieKanaal.EMAIL, "test@example.nl") }
        verify { verzendService.verzendNotificatie(bericht, NotificatieKanaal.SMS, "+31612345678") }
    }

    @Test
    fun `verwerkBerichtOntvangen slaat over wanneer profiel niet gevonden (404)`() {
        val bericht = createTestBericht()
        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } throws
            WebApplicationException(404)

        service.verwerkBerichtOntvangen(bericht)

        verify(exactly = 0) { verzendService.verzendNotificatie(any(), any(), any()) }
    }

    @Test
    fun `verwerkBerichtOntvangen gooit door bij serverfout (5xx)`() {
        val bericht = createTestBericht()
        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } throws
            WebApplicationException(503)

        assertThrows<WebApplicationException> {
            service.verwerkBerichtOntvangen(bericht)
        }
    }

    @Test
    fun `verwerkBerichtOntvangen doet niets wanneer beide kanalen uitgeschakeld`() {
        val bericht = createTestBericht()
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = false,
            smsNotificaties = false
        )

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel

        service.verwerkBerichtOntvangen(bericht)

        verify(exactly = 0) { verzendService.verzendNotificatie(any(), any(), any()) }
    }

    @Test
    fun `verwerkBerichtOntvangen verzendt niet bij emailNotificaties true maar emailAdres null`() {
        val bericht = createTestBericht()
        val profiel = mockk<Profiel> {
            every { emailNotificaties } returns true
            every { emailAdres } returns null
            every { smsNotificaties } returns false
            every { telefoonnummer } returns null
        }

        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } returns profiel

        service.verwerkBerichtOntvangen(bericht)

        verify(exactly = 0) { verzendService.verzendNotificatie(any(), any(), any()) }
    }

    @Test
    fun `verwerkBerichtOntvangen logt en gooit door bij ProcessingException`() {
        val bericht = createTestBericht()
        every { profielClient.haalProfiel("999999999", OntvangerIdType.BSN) } throws
            ProcessingException("Connection refused")

        assertThrows<ProcessingException> {
            service.verwerkBerichtOntvangen(bericht)
        }
    }

    @Test
    fun `haalStatus slaagt ook bij LDV logging fout`() {
        val notificatieId = UUID.randomUUID()
        val entity = createTestEntity(notificatieId)
        every { notificatieRepository.vindOpId(notificatieId) } returns entity
        every { ldvLogger.logVerwerking(any()) } throws RuntimeException("LDV onbereikbaar")

        val status = service.haalStatus(notificatieId)

        assertEquals(notificatieId, status.notificatieId)
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
