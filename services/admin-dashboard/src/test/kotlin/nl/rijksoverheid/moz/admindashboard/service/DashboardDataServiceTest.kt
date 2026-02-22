package nl.rijksoverheid.moz.admindashboard.service

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.moz.client.*
import nl.rijksoverheid.moz.common.model.*
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardDataServiceTest {

    private val fbsClient = mockk<FbsClient>()
    private val berichtenClient = mockk<BerichtenClient>()
    private val notificatieClient = mockk<NotificatieClient>()
    private val bereikbaarheidClient = mockk<BereikbaarheidClient>()
    private val profielClient = mockk<NotificatieprofielClient>()

    private val service = DashboardDataService(fbsClient)

    init {
        every { fbsClient.berichten() } returns berichtenClient
        every { fbsClient.notificaties() } returns notificatieClient
        every { fbsClient.bereikbaarheid() } returns bereikbaarheidClient
        every { fbsClient.profielen() } returns profielClient
    }

    @Test
    fun `haalBerichten retourneert pagina bij succes`() {
        val pagina = Page(
            resultaten = listOf(testBericht()),
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = 1,
            totaalElementen = 1
        )
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 20) } returns pagina

        val result = service.haalBerichten()

        assertEquals(1, result.resultaten.size)
        assertEquals(1L, result.totaalElementen)
    }

    @Test
    fun `haalBerichten retourneert lege pagina bij FbsException`() {
        every { berichtenClient.lijstBerichten(page = 1, pageSize = 20) } throws
            FbsException("Service niet bereikbaar")

        val result = service.haalBerichten()

        assertTrue(result.resultaten.isEmpty())
        assertEquals(0L, result.totaalElementen)
    }

    @Test
    fun `haalBericht retourneert bericht bij succes`() {
        val bericht = testBericht()
        every { berichtenClient.haalBericht(bericht.id) } returns bericht

        val result = service.haalBericht(bericht.id)

        assertEquals(bericht.id, result?.id)
    }

    @Test
    fun `haalBericht retourneert null bij FbsException`() {
        val id = UUID.randomUUID()
        every { berichtenClient.haalBericht(id) } throws FbsException("Niet gevonden", statusCode = 404)

        val result = service.haalBericht(id)

        assertNull(result)
    }

    @Test
    fun `haalNotificatieStatus retourneert status bij succes`() {
        val id = UUID.randomUUID()
        val status = NotificatieStatus(
            notificatieId = id,
            status = NotificatieStatusWaarde.VERZONDEN,
            verzondenOp = Instant.now()
        )
        every { notificatieClient.haalNotificatieStatus(id) } returns status

        val result = service.haalNotificatieStatus(id)

        assertEquals(NotificatieStatusWaarde.VERZONDEN, result?.status)
    }

    @Test
    fun `haalNotificatieStatus retourneert null bij FbsException`() {
        val id = UUID.randomUUID()
        every { notificatieClient.haalNotificatieStatus(id) } throws
            FbsException("Service niet bereikbaar")

        val result = service.haalNotificatieStatus(id)

        assertNull(result)
    }

    @Test
    fun `haalBereikbaarheid retourneert null bij FbsException`() {
        every { bereikbaarheidClient.haalBereikbaarheid("123456789", OntvangerIdType.BSN) } throws
            FbsException("Niet gevonden", statusCode = 404)

        val result = service.haalBereikbaarheid("123456789", OntvangerIdType.BSN)

        assertNull(result)
    }

    @Test
    fun `haalProfiel retourneert null bij FbsException`() {
        every { profielClient.haalProfiel("123456789", OntvangerIdType.BSN) } throws
            FbsException("Niet gevonden", statusCode = 404)

        val result = service.haalProfiel("123456789", OntvangerIdType.BSN)

        assertNull(result)
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
