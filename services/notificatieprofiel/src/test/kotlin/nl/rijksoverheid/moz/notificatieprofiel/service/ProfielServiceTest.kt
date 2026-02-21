package nl.rijksoverheid.moz.notificatieprofiel.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.NotificatieFrequentie
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.notificatieprofiel.entity.ProfielEntity
import nl.rijksoverheid.moz.notificatieprofiel.exception.ProfielNietGevondenException
import nl.rijksoverheid.moz.notificatieprofiel.repository.ProfielRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProfielServiceTest {

    private val profielRepository = mockk<ProfielRepository>()
    private val ldvLogger = mockk<LdvLogger>()

    private val service = ProfielService(profielRepository, ldvLogger)

    init {
        every { ldvLogger.logVerwerking(any()) } just Runs
        every { ldvLogger.withinVerwerking(any(), any<() -> Any>()) } answers {
            val block = secondArg<() -> Any>()
            block()
        }
    }

    @Test
    fun `haalProfiel geeft profiel terug wanneer gevonden`() {
        val entity = createTestEntity()
        every { profielRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns entity

        val profiel = service.haalProfiel("999999999", OntvangerIdType.BSN)

        assertEquals("999999999", profiel.ontvangerId)
        assertEquals(OntvangerIdType.BSN, profiel.ontvangerIdType)
        assertEquals(true, profiel.emailNotificaties)
        assertEquals("test@example.nl", profiel.emailAdres)
    }

    @Test
    fun `haalProfiel gooit exception wanneer niet gevonden`() {
        every { profielRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns null

        assertThrows<ProfielNietGevondenException> {
            service.haalProfiel("999999999", OntvangerIdType.BSN)
        }
    }

    @Test
    fun `werkProfielBij maakt nieuw profiel aan wanneer niet bestaat`() {
        every { profielRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns null
        every { profielRepository.bewaar(any<ProfielEntity>()) } just Runs

        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = true,
            smsNotificaties = false,
            emailAdres = "nieuw@example.nl",
            frequentie = NotificatieFrequentie.DAGELIJKS
        )

        val result = service.werkProfielBij("999999999", OntvangerIdType.BSN, profiel)

        assertEquals("999999999", result.ontvangerId)
        assertEquals(true, result.emailNotificaties)
        assertEquals("nieuw@example.nl", result.emailAdres)
        assertEquals(NotificatieFrequentie.DAGELIJKS, result.frequentie)
        verify { profielRepository.bewaar(any<ProfielEntity>()) }
    }

    @Test
    fun `werkProfielBij werkt bestaand profiel bij`() {
        val entity = createTestEntity()
        every { profielRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns entity
        every { profielRepository.bewaar(any<ProfielEntity>()) } just Runs

        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = false,
            smsNotificaties = true,
            telefoonnummer = "+31612345678",
            frequentie = NotificatieFrequentie.WEKELIJKS
        )

        val result = service.werkProfielBij("999999999", OntvangerIdType.BSN, profiel)

        assertEquals(false, result.emailNotificaties)
        assertEquals(true, result.smsNotificaties)
        assertEquals("+31612345678", result.telefoonnummer)
        assertEquals(NotificatieFrequentie.WEKELIJKS, result.frequentie)
    }

    @Test
    fun `werkProfielBij wijst niet-overeenkomend ontvangerId af`() {
        val profiel = Profiel(
            ontvangerId = "111111111",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = false,
            smsNotificaties = false
        )

        assertThrows<IllegalArgumentException> {
            service.werkProfielBij("999999999", OntvangerIdType.BSN, profiel)
        }
    }

    @Test
    fun `werkProfielBij wijst niet-overeenkomend ontvangerIdType af`() {
        val profiel = Profiel(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.KVK,
            emailNotificaties = false,
            smsNotificaties = false
        )

        assertThrows<IllegalArgumentException> {
            service.werkProfielBij("999999999", OntvangerIdType.BSN, profiel)
        }
    }

    private fun createTestEntity() = ProfielEntity(
        ontvangerId = "999999999",
        ontvangerIdType = OntvangerIdType.BSN,
        emailNotificaties = true,
        smsNotificaties = false,
        emailAdres = "test@example.nl",
        frequentie = NotificatieFrequentie.DIRECT
    )
}
