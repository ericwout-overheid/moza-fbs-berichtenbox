package nl.rijksoverheid.moz.berichtenlijst.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import nl.rijksoverheid.moz.berichtenlijst.client.BerichtenmagazijnClient
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Page
import nl.rijksoverheid.moz.ldv.LdvLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import jakarta.ws.rs.ProcessingException
import java.time.Instant
import java.util.UUID

class BerichtenlijstServiceTest {

    private val berichtenmagazijnClient = mockk<BerichtenmagazijnClient>()
    private val ldvLogger = mockk<LdvLogger>()

    private val service = BerichtenlijstService(
        berichtenmagazijnClient,
        ldvLogger,
        "http://localhost:8080"
    )

    init {
        every { ldvLogger.logVerwerking(any()) } just Runs
    }

    @Test
    fun `haalBerichtenlijst geeft gemapte records terug`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, null)
        } returns createTestPage()

        val result = service.haalBerichtenlijst(OntvangerIdType.BSN, "999999999", 1, 20)

        assertEquals(2, result.resultaten.size)
        assertEquals("Belastingaanslag 2025", result.resultaten[0].onderwerp)
        assertTrue(result.resultaten[0].magazijnUrl.contains("/api/v1/berichten/"))
    }

    @Test
    fun `zoekBerichten stuurt zoekterm naar server`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, "belasting")
        } returns createFilteredPage("belasting")

        val result = service.zoekBerichten(OntvangerIdType.BSN, "999999999", "belasting", 1, 20)

        assertEquals(1, result.resultaten.size)
        assertEquals("Belastingaanslag 2025", result.resultaten[0].onderwerp)
    }

    @Test
    fun `zoekBerichten met zoekterm korter dan 2 tekens geeft fout`() {
        assertThrows<IllegalArgumentException> {
            service.zoekBerichten(OntvangerIdType.BSN, "999999999", "a", 1, 20)
        }
    }

    @Test
    fun `zoekBerichten met zoekterm van 2 tekens slaagt`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, "be")
        } returns createFilteredPage("be")

        val result = service.zoekBerichten(OntvangerIdType.BSN, "999999999", "be", 1, 20)

        assertEquals(1, result.resultaten.size)
    }

    @Test
    fun `haalBerichtenlijst kapt pageSize af op maximum`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, FbsConstants.MAX_PAGE_SIZE, null)
        } returns createTestPage()

        val result = service.haalBerichtenlijst(OntvangerIdType.BSN, "999999999", 1, 999)

        assertEquals(2, result.resultaten.size)
    }

    @Test
    fun `haalBerichtenlijst corrigeert page naar minimum 1`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, null)
        } returns createTestPage()

        val result = service.haalBerichtenlijst(OntvangerIdType.BSN, "999999999", 0, 20)

        assertEquals(2, result.resultaten.size)
    }

    @Test
    fun `haalBerichtenlijst propageert ProcessingException van REST client`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, null)
        } throws ProcessingException("Connection refused")

        assertThrows<ProcessingException> {
            service.haalBerichtenlijst(OntvangerIdType.BSN, "999999999", 1, 20)
        }
    }

    @Test
    fun `zoekBerichten geeft lege resultaten wanneer server niets vindt`() {
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, "onbestaand")
        } returns Page(
            resultaten = emptyList(),
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = 0,
            totaalElementen = 0L
        )

        val result = service.zoekBerichten(OntvangerIdType.BSN, "999999999", "onbestaand", 1, 20)

        assertEquals(0, result.resultaten.size)
        assertEquals(0L, result.totaalElementen)
        assertEquals(0, result.totaalPaginas)
    }

    @Test
    fun `haalBerichtenlijst slaagt ook bij LDV logging fout`() {
        every { ldvLogger.logVerwerking(any()) } throws RuntimeException("LDV onbereikbaar")
        every {
            berichtenmagazijnClient.lijstBerichten(OntvangerIdType.BSN, "999999999", 1, 20, null)
        } returns createTestPage()

        val result = service.haalBerichtenlijst(OntvangerIdType.BSN, "999999999", 1, 20)

        assertEquals(2, result.resultaten.size)
    }

    private fun createTestPage(): Page<Bericht> {
        val berichten = listOf(
            Bericht(
                id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                afzenderOin = "00000001234567890000",
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "999999999",
                onderwerp = "Belastingaanslag 2025",
                inhoud = "Uw belastingaanslag voor het jaar 2025.",
                status = BerichtStatus.NIEUW,
                aangemaaktOp = Instant.parse("2025-01-15T10:00:00Z")
            ),
            Bericht(
                id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                afzenderOin = "00000009876543210000",
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "999999999",
                onderwerp = "Vergunning verleend",
                inhoud = "Uw vergunningaanvraag is goedgekeurd.",
                status = BerichtStatus.GELEZEN,
                aangemaaktOp = Instant.parse("2025-02-01T14:30:00Z"),
                gelezenOp = Instant.parse("2025-02-02T09:00:00Z")
            )
        )

        return Page(
            resultaten = berichten,
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = 1,
            totaalElementen = berichten.size.toLong()
        )
    }

    private fun createFilteredPage(zoekterm: String): Page<Bericht> {
        val allBerichten = createTestPage().resultaten
        val filtered = allBerichten.filter { it.onderwerp.contains(zoekterm, ignoreCase = true) }
        return Page(
            resultaten = filtered,
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = if (filtered.isEmpty()) 0 else 1,
            totaalElementen = filtered.size.toLong()
        )
    }
}
