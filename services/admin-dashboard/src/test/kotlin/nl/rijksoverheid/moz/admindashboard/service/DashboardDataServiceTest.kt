package nl.rijksoverheid.moz.admindashboard.service

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.moz.client.*
import nl.rijksoverheid.moz.common.model.*
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardDataServiceTest {

    private val fbsClient = mockk<FbsClient>()
    private val berichtenClient = mockk<BerichtenClient>()

    private val service = DashboardDataService(fbsClient)

    init {
        every { fbsClient.berichten() } returns berichtenClient
    }

    // --- DashboardResult contract tests ---

    @Test
    fun `DashboardResult ok heeft isFout false en null foutmelding`() {
        val result = DashboardResult.ok("data")
        assertFalse(result.isFout)
        assertNull(result.foutmelding)
        assertEquals("data", result.data)
    }

    @Test
    fun `DashboardResult fout heeft isFout true en behoudt fallback`() {
        val result = DashboardResult.fout("Foutmelding", "fallback")
        assertTrue(result.isFout)
        assertEquals("Foutmelding", result.foutmelding)
        assertEquals("fallback", result.data)
    }

    // --- haalBerichten ---

    @Test
    fun `haalBerichten retourneert pagina bij succes`() {
        val pagina = Page(
            resultaten = listOf(testBericht()),
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = 1,
            totaalElementen = 1
        )
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 20, status = null) } returns pagina

        val result = service.haalBerichten()

        assertFalse(result.isFout)
        assertEquals(1, result.data.resultaten.size)
        assertEquals(1L, result.data.totaalElementen)
    }

    @Test
    fun `haalBerichten geeft page en pageSize door aan client`() {
        val pagina = Page(
            resultaten = emptyList<Bericht>(),
            pagina = 3,
            paginaGrootte = 10,
            totaalPaginas = 5,
            totaalElementen = 42
        )
        every { berichtenClient.lijstBerichten(page = 3, pageSize = 10, status = null) } returns pagina

        val result = service.haalBerichten(page = 3, pageSize = 10)

        assertFalse(result.isFout)
        assertEquals(3, result.data.pagina)
        assertEquals(10, result.data.paginaGrootte)
        assertEquals(42L, result.data.totaalElementen)
    }

    @Test
    fun `haalBerichten geeft status filter door aan client`() {
        val pagina = Page(
            resultaten = listOf(testBericht()),
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = 1,
            totaalElementen = 1
        )
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 20, status = BerichtStatus.NIEUW) } returns pagina

        val result = service.haalBerichten(status = BerichtStatus.NIEUW)

        assertFalse(result.isFout)
        assertEquals(1, result.data.resultaten.size)
    }

    @Test
    fun `haalBerichten retourneert fout met lege pagina bij FbsException`() {
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 20, status = null) } throws
            FbsException("Service niet bereikbaar")

        val result = service.haalBerichten()

        assertTrue(result.isFout)
        assertNotNull(result.foutmelding)
        assertTrue(result.data.resultaten.isEmpty())
        assertEquals(0L, result.data.totaalElementen)
    }

    @Test
    fun `haalBerichten fout fallback behoudt meegegeven pageSize`() {
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 50, status = null) } throws
            FbsException("Service niet bereikbaar")

        val result = service.haalBerichten(pageSize = 50)

        assertTrue(result.isFout)
        assertEquals(50, result.data.paginaGrootte)
    }

    // --- haalBericht ---

    @Test
    fun `haalBericht retourneert bericht bij succes`() {
        val bericht = testBericht()
        every { berichtenClient.haalBericht(bericht.id) } returns bericht

        val result = service.haalBericht(bericht.id)

        assertFalse(result.isFout)
        assertEquals(bericht.id, result.data?.id)
    }

    @Test
    fun `haalBericht retourneert fout bij FbsException`() {
        val id = UUID.randomUUID()
        every { berichtenClient.haalBericht(id) } throws FbsException("Niet gevonden", statusCode = 404)

        val result = service.haalBericht(id)

        assertTrue(result.isFout)
        assertNull(result.data)
    }

    // --- Exception propagation ---

    @Test
    fun `haalBerichten laat niet-FbsException doorbubbelen`() {
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 20, status = null) } throws
            IllegalStateException("Onverwachte fout")

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            service.haalBerichten()
        }
    }

    private fun testBericht() = Bericht(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        afzenderOin = "00000001234567890000",
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "123456789",
        onderwerp = "Test",
        inhoud = "Inhoud",
        status = BerichtStatus.NIEUW,
        aangemaaktOp = Instant.parse("2025-01-01T00:00:00Z")
    )
}
