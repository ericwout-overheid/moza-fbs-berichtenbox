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

    // --- mapPortToNaam ---

    @Test
    fun `mapPortToNaam retourneert Berichtenmagazijn voor poort 8080`() {
        assertEquals("Berichtenmagazijn", ServiceHealthChecker.mapPortToNaam("http://localhost:8080"))
    }

    @Test
    fun `mapPortToNaam retourneert Berichtenlijst voor poort 8081`() {
        assertEquals("Berichtenlijst", ServiceHealthChecker.mapPortToNaam("http://localhost:8081"))
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

    // --- checkAll single URL ---

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
            assertNotNull(results[0].responseTimeMs)
            assertTrue(results[0].responseTimeMs!! >= 0)
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
    fun `checkAll retourneert beschikbaar true voor 299 response`() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/q/health") { exchange ->
            exchange.sendResponseHeaders(299, -1)
            exchange.responseBody.close()
        }
        server.start()
        val port = server.address.port

        try {
            val checker = ServiceHealthChecker(listOf("http://localhost:$port"))
            val results = checker.checkAll()

            assertEquals(1, results.size)
            assertTrue(results[0].beschikbaar)
            assertEquals(299, results[0].statusCode)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `checkAll retourneert beschikbaar false voor 300 response`() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/q/health") { exchange ->
            exchange.sendResponseHeaders(300, -1)
            exchange.responseBody.close()
        }
        server.start()
        val port = server.address.port

        try {
            val checker = ServiceHealthChecker(listOf("http://localhost:$port"))
            val results = checker.checkAll()

            assertEquals(1, results.size)
            assertFalse(results[0].beschikbaar)
            assertEquals(300, results[0].statusCode)
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
        assertNull(results[0].responseTimeMs)
    }

    @Test
    fun `checkAll met lege URL lijst retourneert lege lijst`() {
        val checker = ServiceHealthChecker(emptyList())

        val results = checker.checkAll()

        assertTrue(results.isEmpty())
    }

    // --- checkAll multiple URLs ---

    @Test
    fun `checkAll retourneert mix van beschikbare en onbereikbare services`() {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/q/health") { exchange ->
            exchange.sendResponseHeaders(200, 0)
            exchange.responseBody.close()
        }
        server.start()
        val port = server.address.port

        try {
            val checker = ServiceHealthChecker(listOf(
                "http://localhost:$port",
                "http://localhost:19998",
                "http://localhost:$port"
            ))
            val results = checker.checkAll()

            assertEquals(3, results.size)
            val upCount = results.count { it.beschikbaar }
            val downCount = results.count { !it.beschikbaar }
            assertEquals(2, upCount)
            assertEquals(1, downCount)
        } finally {
            server.stop(0)
        }
    }

    // --- ServiceStatus invariants ---

    @Test
    fun `ServiceStatus beschikbaar vereist statusCode`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ServiceStatus(naam = "Test", url = "http://test", beschikbaar = true, statusCode = null)
        }
    }

    @Test
    fun `ServiceStatus beschikbaar mag geen foutmelding hebben`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ServiceStatus(naam = "Test", url = "http://test", beschikbaar = true, statusCode = 200, foutmelding = "fout")
        }
    }
}
