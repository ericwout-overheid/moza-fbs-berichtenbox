package nl.rijksoverheid.moz.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.NotificatieFrequentie
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificatieprofielClientTest {

    private val httpClient = mockk<HttpClient>()
    private val http = FbsHttpSupport.create(
        bearerToken = "test-token",
        connectTimeout = Duration.ofSeconds(5),
        requestTimeout = Duration.ofSeconds(30),
        httpClient = httpClient
    )
    private val client = NotificatieprofielClient("http://localhost:8083", http)

    @Test
    fun `haalProfiel verstuurt GET met ontvangerId en type`() {
        mockResponse(200, profielJson())

        val requestSlot = slot<HttpRequest>()
        val result = client.haalProfiel("123456789", OntvangerIdType.BSN)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        val uri = requestSlot.captured.uri().toString()
        assertTrue(uri.contains("/api/v1/profielen/123456789"))
        assertTrue(uri.contains("ontvangerIdType=BSN"))
        assertEquals("123456789", result.ontvangerId)
        assertEquals(true, result.emailNotificaties)
    }

    @Test
    fun `werkProfielBij verstuurt PUT`() {
        mockResponse(200, profielJson())

        val requestSlot = slot<HttpRequest>()
        val profiel = Profiel(
            ontvangerId = "123456789",
            ontvangerIdType = OntvangerIdType.BSN,
            emailNotificaties = true,
            smsNotificaties = false,
            emailAdres = "test@example.com"
        )
        val result = client.werkProfielBij("123456789", OntvangerIdType.BSN, profiel)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("PUT", requestSlot.captured.method())
        assertEquals("123456789", result.ontvangerId)
    }

    @Test
    fun `fout HTTP status gooit FbsException`() {
        mockResponse(404, """{"type":"about:blank","title":"Not Found","status":404}""",
            contentType = "application/problem+json")

        val exception = assertThrows<FbsException> {
            client.haalProfiel("999", OntvangerIdType.BSN)
        }
        assertEquals(404, exception.statusCode)
    }

    private fun profielJson() = """
        {
            "ontvangerId": "123456789",
            "ontvangerIdType": "BSN",
            "emailNotificaties": true,
            "smsNotificaties": false,
            "emailAdres": "test@example.com",
            "frequentie": "DIRECT"
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
