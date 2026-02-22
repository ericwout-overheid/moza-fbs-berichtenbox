package nl.rijksoverheid.moz.admindashboard.service

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    fun `mapPortToNaam retourneert hostname voor onbekende poort`() {
        assertEquals("localhost", ServiceHealthChecker.mapPortToNaam("http://localhost:9999"))
    }

    @Test
    fun `mapPortToNaam retourneert Onbekend voor ongeldige URL`() {
        assertEquals("Onbekend", ServiceHealthChecker.mapPortToNaam("ongeldige url met spaties"))
    }

    @Test
    fun `mapPortToNaam retourneert hostname voor URL zonder bekende poort`() {
        assertEquals("example.com", ServiceHealthChecker.mapPortToNaam("http://example.com:9999"))
    }

    @Test
    fun `checkAll retourneert beschikbaar true voor bereikbare service`() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/q/health") { exchange ->
            exchange.sendResponseHeaders(200, 0)
            exchange.responseBody.close()
        }
        server.start()
        val port = server.address.port

        try {
            val checker = ServiceHealthChecker(listOf("http://localhost:$port"))
            val results = checker.checkAll()

            assertEquals(1, results.size)
            assertTrue(results[0].beschikbaar)
            assertEquals(200, results[0].statusCode)
            assertTrue(results[0].responseTimeMs >= 0)
            assertNull(results[0].foutmelding)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `checkAll retourneert beschikbaar false voor 503 response`() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/q/health") { exchange ->
            exchange.sendResponseHeaders(503, -1)
            exchange.responseBody.close()
        }
        server.start()
        val port = server.address.port

        try {
            val checker = ServiceHealthChecker(listOf("http://localhost:$port"))
            val results = checker.checkAll()

            assertEquals(1, results.size)
            assertFalse(results[0].beschikbaar)
            assertEquals(503, results[0].statusCode)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `checkAll retourneert beschikbaar false voor onbereikbare service`() {
        val checker = ServiceHealthChecker(listOf("http://localhost:19999"))

        val results = checker.checkAll()

        assertEquals(1, results.size)
        assertFalse(results[0].beschikbaar)
        assertNotNull(results[0].foutmelding)
    }

    @Test
    fun `checkAll met lege URL lijst retourneert lege lijst`() {
        val checker = ServiceHealthChecker(emptyList())

        val results = checker.checkAll()

        assertTrue(results.isEmpty())
    }
}
