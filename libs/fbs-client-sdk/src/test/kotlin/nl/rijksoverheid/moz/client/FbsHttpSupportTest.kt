package nl.rijksoverheid.moz.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows

class FbsHttpSupportTest {

    private val httpClient = mockk<HttpClient>()
    private val support = FbsHttpSupport.create(
        bearerToken = "test-token",
        connectTimeout = Duration.ofSeconds(5),
        requestTimeout = Duration.ofSeconds(30),
        httpClient = httpClient
    )

    @Test
    fun `authorization header wordt gezet met bearer token`() {
        mockResponse(200, """{"title":"test","status":200}""")

        val requestSlot = slot<HttpRequest>()
        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        support.execute(request, Map::class.java)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals(
            "Bearer test-token",
            requestSlot.captured.headers().firstValue("Authorization").get()
        )
    }

    @Test
    fun `API-Version header wordt gezet`() {
        mockResponse(200, """{}""")

        val requestSlot = slot<HttpRequest>()
        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        support.execute(request, Map::class.java)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals(
            "1.0.0",
            requestSlot.captured.headers().firstValue("API-Version").get()
        )
    }

    @Test
    fun `traceparent header wordt meegezonden`() {
        mockResponse(200, """{}""")

        val requestSlot = slot<HttpRequest>()
        val request = support.requestBuilder(
            URI.create("http://localhost/test"),
            traceparent = "00-trace-span-01"
        ).GET().build()

        support.execute(request, Map::class.java)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals(
            "00-trace-span-01",
            requestSlot.captured.headers().firstValue("traceparent").get()
        )
    }

    @Test
    fun `geen authorization header zonder bearer token`() {
        val noAuthSupport = FbsHttpSupport.create(
            bearerToken = null,
            connectTimeout = Duration.ofSeconds(5),
            requestTimeout = Duration.ofSeconds(30),
            httpClient = httpClient
        )
        mockResponse(200, """{}""")

        val requestSlot = slot<HttpRequest>()
        val request = noAuthSupport.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        noAuthSupport.execute(request, Map::class.java)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertTrue(requestSlot.captured.headers().firstValue("Authorization").isEmpty)
    }

    @Test
    fun `onverwachte statuscode gooit FbsException`() {
        mockResponse(500, """{"error":"server error"}""")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        val exception = assertThrows<FbsException> {
            support.execute(request, Map::class.java)
        }
        assertEquals(500, exception.statusCode)
    }

    @Test
    fun `fout statuscode bevat response body in bericht`() {
        mockResponse(502, "Bad Gateway: upstream unavailable")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        val exception = assertThrows<FbsException> {
            support.execute(request, Map::class.java)
        }
        assertEquals(502, exception.statusCode)
        assertTrue(exception.message!!.contains("Bad Gateway: upstream unavailable"))
    }

    @Test
    fun `problem+json response wordt geparsed in exception`() {
        mockResponse(
            400,
            """{"type":"about:blank","title":"Bad Request","status":400,"detail":"Ongeldige invoer"}""",
            contentType = "application/problem+json"
        )

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        val exception = assertThrows<FbsException> {
            support.execute(request, Map::class.java)
        }
        assertEquals(400, exception.statusCode)
        assertNotNull(exception.problemDetail)
        assertEquals("Ongeldige invoer", exception.problemDetail!!.detail)
        assertEquals("Bad Request", exception.problemDetail!!.title)
    }

    @Test
    fun `verbindingsfout gooit FbsException zonder statusCode`() {
        every {
            httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())
        } throws java.net.ConnectException("Connection refused")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        val exception = assertThrows<FbsException> {
            support.execute(request, Map::class.java)
        }
        assertNull(exception.statusCode)
    }

    @Test
    fun `InterruptedException herstelt interrupt flag en gooit FbsException`() {
        every {
            httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())
        } throws InterruptedException("interrupted")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        val exception = assertThrows<FbsException> {
            support.execute(request, Map::class.java)
        }
        assertNull(exception.statusCode)
        assertTrue(Thread.currentThread().isInterrupted)
        // Reset interrupt flag voor overige tests
        Thread.interrupted()
    }

    @Test
    fun `ongeldige JSON response gooit FbsException`() {
        mockResponse(200, "dit is geen json")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .GET()
            .build()

        val exception = assertThrows<FbsException> {
            support.execute(request, String::class.java)
        }
        assertEquals(200, exception.statusCode)
        assertNotNull(exception.cause)
    }

    @Test
    fun `executeNoContent accepteert 204 response`() {
        mockResponse(204, "")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .DELETE()
            .build()

        support.executeNoContent(request)
    }

    @Test
    fun `executeNoContent gooit FbsException bij onverwachte status`() {
        mockResponse(200, """{"result":"ok"}""")

        val request = support.requestBuilder(URI.create("http://localhost/test"))
            .DELETE()
            .build()

        val exception = assertThrows<FbsException> {
            support.executeNoContent(request)
        }
        assertEquals(200, exception.statusCode)
    }

    @Test
    fun `jsonBody gooit FbsException bij serialisatiefout`() {
        // Een object dat niet te serialiseren is
        val exception = assertThrows<FbsException> {
            support.jsonBody(object { val self: Any = this })
        }
        assertNull(exception.statusCode)
        assertTrue(exception.message!!.contains("serialiseren"))
    }

    @Suppress("UNCHECKED_CAST")
    private fun mockResponse(statusCode: Int, body: String, contentType: String? = null) {
        val response = mockk<HttpResponse<String>>()
        every { response.statusCode() } returns statusCode
        every { response.body() } returns body

        val headers = mockk<java.net.http.HttpHeaders>()
        every { headers.firstValue("Content-Type") } returns java.util.Optional.ofNullable(contentType)
        every { response.headers() } returns headers

        every {
            httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())
        } returns response as HttpResponse<String>
    }
}
