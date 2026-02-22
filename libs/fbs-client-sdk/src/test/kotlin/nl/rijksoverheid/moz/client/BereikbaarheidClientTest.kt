package nl.rijksoverheid.moz.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.Bereikbaarheid
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BereikbaarheidClientTest {

    private val httpClient = mockk<HttpClient>()
    private val http = FbsHttpSupport.create(
        bearerToken = "test-token",
        connectTimeout = Duration.ofSeconds(5),
        requestTimeout = Duration.ofSeconds(30),
        httpClient = httpClient
    )
    private val client = BereikbaarheidClient("http://localhost:8084", http)

    @Test
    fun `haalBereikbaarheid verstuurt GET met ontvangerId en type`() {
        mockResponse(200, bereikbaarheidJson())

        val requestSlot = slot<HttpRequest>()
        val result = client.haalBereikbaarheid("123456789", OntvangerIdType.BSN)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        val uri = requestSlot.captured.uri().toString()
        assertTrue(uri.contains("/api/v1/bereikbaarheid/123456789"))
        assertTrue(uri.contains("ontvangerIdType=BSN"))
        assertEquals(true, result.digitaalBereikbaar)
    }

    @Test
    fun `registreerBereikbaarheid verstuurt PUT`() {
        mockResponse(200, bereikbaarheidJson())

        val requestSlot = slot<HttpRequest>()
        val bereikbaarheid = Bereikbaarheid(
            ontvangerId = "123456789",
            ontvangerIdType = OntvangerIdType.BSN,
            digitaalBereikbaar = true,
            registratieDatum = Instant.parse("2025-01-01T00:00:00Z")
        )
        val result = client.registreerBereikbaarheid("123456789", OntvangerIdType.BSN, bereikbaarheid)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("PUT", requestSlot.captured.method())
        assertEquals(true, result.digitaalBereikbaar)
    }

    @Test
    fun `fout HTTP status gooit FbsException`() {
        mockResponse(404, """{"type":"about:blank","title":"Not Found","status":404}""",
            contentType = "application/problem+json")

        val exception = assertThrows<FbsException> {
            client.haalBereikbaarheid("999", OntvangerIdType.BSN)
        }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `traceparent header wordt doorgegeven`() {
        mockResponse(200, bereikbaarheidJson())

        val requestSlot = slot<HttpRequest>()
        client.haalBereikbaarheid("123456789", OntvangerIdType.BSN, traceparent = "00-abc-def-01")

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals(
            "00-abc-def-01",
            requestSlot.captured.headers().firstValue("traceparent").get()
        )
    }

    private fun bereikbaarheidJson() = """
        {
            "ontvangerId": "123456789",
            "ontvangerIdType": "BSN",
            "digitaalBereikbaar": true,
            "registratieDatum": "2025-01-01T00:00:00Z"
        }
    """.trimIndent()

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
