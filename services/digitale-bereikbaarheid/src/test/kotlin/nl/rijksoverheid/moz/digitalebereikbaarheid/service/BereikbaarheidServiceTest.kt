package nl.rijksoverheid.moz.digitalebereikbaarheid.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.Bereikbaarheid
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.digitalebereikbaarheid.entity.BereikbaarheidEntity
import nl.rijksoverheid.moz.digitalebereikbaarheid.exception.BereikbaarheidNietGevondenException
import nl.rijksoverheid.moz.digitalebereikbaarheid.repository.BereikbaarheidRepository
import nl.rijksoverheid.moz.ldv.LdvLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class BereikbaarheidServiceTest {

    private val bereikbaarheidRepository = mockk<BereikbaarheidRepository>()
    private val ldvLogger = mockk<LdvLogger>()

    private val service = BereikbaarheidService(bereikbaarheidRepository, ldvLogger)

    init {
        every { ldvLogger.logVerwerking(any()) } just Runs
        every { ldvLogger.withinVerwerking(any(), any<() -> Any>()) } answers {
            val block = secondArg<() -> Any>()
            block()
        }
    }

    @Test
    fun `haalBereikbaarheid geeft bereikbaarheid terug wanneer gevonden`() {
        val entity = createTestEntity()
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns entity

        val bereikbaarheid = service.haalBereikbaarheid("999999999", OntvangerIdType.BSN)

        assertEquals("999999999", bereikbaarheid.ontvangerId)
        assertEquals(OntvangerIdType.BSN, bereikbaarheid.ontvangerIdType)
        assertEquals(true, bereikbaarheid.digitaalBereikbaar)
        assertNull(bereikbaarheid.intrekkingsDatum)
    }

    @Test
    fun `haalBereikbaarheid gooit exception wanneer niet gevonden`() {
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns null

        assertThrows<BereikbaarheidNietGevondenException> {
            service.haalBereikbaarheid("999999999", OntvangerIdType.BSN)
        }
    }

    @Test
    fun `registreerBereikbaarheid maakt nieuwe registratie aan wanneer niet bestaat`() {
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns null
        every { bereikbaarheidRepository.bewaar(any<BereikbaarheidEntity>()) } just Runs

        val bereikbaarheid = Bereikbaarheid(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            digitaalBereikbaar = true,
            registratieDatum = Instant.now()
        )

        val result = service.registreerBereikbaarheid("999999999", OntvangerIdType.BSN, bereikbaarheid)

        assertEquals("999999999", result.ontvangerId)
        assertEquals(true, result.digitaalBereikbaar)
        assertNull(result.intrekkingsDatum)
        verify { bereikbaarheidRepository.bewaar(any<BereikbaarheidEntity>()) }
    }

    @Test
    fun `registreerBereikbaarheid werkt bestaande registratie bij`() {
        val entity = createTestEntity()
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns entity
        every { bereikbaarheidRepository.bewaar(any<BereikbaarheidEntity>()) } just Runs

        val bereikbaarheid = Bereikbaarheid(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            digitaalBereikbaar = false,
            registratieDatum = Instant.now()
        )

        val result = service.registreerBereikbaarheid("999999999", OntvangerIdType.BSN, bereikbaarheid)

        assertEquals(false, result.digitaalBereikbaar)
        assertNotNull(result.intrekkingsDatum)
    }

    @Test
    fun `registreerBereikbaarheid zet intrekkingsDatum bij digitaalBereikbaar false`() {
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns null
        every { bereikbaarheidRepository.bewaar(any<BereikbaarheidEntity>()) } just Runs

        val bereikbaarheid = Bereikbaarheid(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            digitaalBereikbaar = false,
            registratieDatum = Instant.now()
        )

        val result = service.registreerBereikbaarheid("999999999", OntvangerIdType.BSN, bereikbaarheid)

        assertEquals(false, result.digitaalBereikbaar)
        assertNotNull(result.intrekkingsDatum)
    }

    @Test
    fun `registreerBereikbaarheid wist intrekkingsDatum bij digitaalBereikbaar true`() {
        val entity = createTestEntity(digitaalBereikbaar = false, intrekkingsDatum = Instant.now())
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns entity
        every { bereikbaarheidRepository.bewaar(any<BereikbaarheidEntity>()) } just Runs

        val bereikbaarheid = Bereikbaarheid(
            ontvangerId = "999999999",
            ontvangerIdType = OntvangerIdType.BSN,
            digitaalBereikbaar = true,
            registratieDatum = Instant.now()
        )

        val result = service.registreerBereikbaarheid("999999999", OntvangerIdType.BSN, bereikbaarheid)

        assertEquals(true, result.digitaalBereikbaar)
        assertNull(result.intrekkingsDatum)
    }

    @Test
    fun `registreerBereikbaarheid wijst niet-overeenkomend ontvangerId af`() {
        val bereikbaarheid = Bereikbaarheid(
            ontvangerId = "111111111",
            ontvangerIdType = OntvangerIdType.BSN,
            digitaalBereikbaar = true,
            registratieDatum = Instant.now()
        )

        assertThrows<IllegalArgumentException> {
            service.registreerBereikbaarheid("999999999", OntvangerIdType.BSN, bereikbaarheid)
        }
    }

    @Test
    fun `haalBereikbaarheid slaagt ook bij LDV logging fout`() {
        val entity = createTestEntity()
        every { bereikbaarheidRepository.vindOpOntvanger("999999999", OntvangerIdType.BSN) } returns entity
        every { ldvLogger.logVerwerking(any()) } throws RuntimeException("LDV onbereikbaar")

        val bereikbaarheid = service.haalBereikbaarheid("999999999", OntvangerIdType.BSN)

        assertEquals("999999999", bereikbaarheid.ontvangerId)
    }

    private fun createTestEntity(
        digitaalBereikbaar: Boolean = true,
        intrekkingsDatum: Instant? = null
    ) = BereikbaarheidEntity(
        ontvangerId = "999999999",
        ontvangerIdType = OntvangerIdType.BSN,
        digitaalBereikbaar = digitaalBereikbaar,
        intrekkingsDatum = intrekkingsDatum
    )
}
