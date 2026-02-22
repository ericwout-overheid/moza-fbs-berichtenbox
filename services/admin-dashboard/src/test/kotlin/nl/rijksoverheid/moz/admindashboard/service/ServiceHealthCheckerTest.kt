package nl.rijksoverheid.moz.admindashboard.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServiceHealthCheckerTest {

    @Test
    fun `mapPortToNaam retourneert Berichtenmagazijn voor poort 8080`() {
        assertEquals("Berichtenmagazijn", ServiceHealthChecker.mapPortToNaam("http://localhost:8080"))
    }

    @Test
    fun `mapPortToNaam retourneert Berichtenlijst voor poort 8081`() {
        assertEquals("Berichtenlijst", ServiceHealthChecker.mapPortToNaam("http://localhost:8081"))
    }

    @Test
    fun `mapPortToNaam retourneert Notificatie voor poort 8082`() {
        assertEquals("Notificatie", ServiceHealthChecker.mapPortToNaam("http://localhost:8082"))
    }

    @Test
    fun `mapPortToNaam retourneert Notificatieprofiel voor poort 8083`() {
        assertEquals("Notificatieprofiel", ServiceHealthChecker.mapPortToNaam("http://localhost:8083"))
    }

    @Test
    fun `mapPortToNaam retourneert Digitale Bereikbaarheid voor poort 8084`() {
        assertEquals("Digitale Bereikbaarheid", ServiceHealthChecker.mapPortToNaam("http://localhost:8084"))
    }

    @Test
    fun `mapPortToNaam retourneert Onbekend voor onbekende poort`() {
        assertEquals("Onbekend (9999)", ServiceHealthChecker.mapPortToNaam("http://localhost:9999"))
    }

    @Test
    fun `checkAll retourneert beschikbaar false voor onbereikbare service`() {
        val checker = ServiceHealthChecker(listOf("http://localhost:19999"))

        val results = checker.checkAll()

        assertEquals(1, results.size)
        assertFalse(results[0].beschikbaar)
        assertTrue(results[0].foutmelding != null)
    }
}
