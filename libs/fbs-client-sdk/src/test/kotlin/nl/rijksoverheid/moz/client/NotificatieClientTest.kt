package nl.rijksoverheid.moz.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.NotificatieVerzoek
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificatieClientTest {

    private val httpClient = mockk<HttpClient>()
    private val http = FbsHttpSupport.create(
        bearerToken = "test-token",
        connectTimeout = Duration.ofSeconds(5),
        requestTimeout = Duration.ofSeconds(30),
        httpClient = httpClient
    )
    private val client = NotificatieClient("http://localhost:8082", http)

    private val notificatieId = UUID.fromString("00000000-0000-0000-0000-000000000010")

    @Test
    fun `verstuurNotificatie verstuurt POST en accepteert 202`() {
        mockResponse(202, notificatieJson())

        val requestSlot = slot<HttpRequest>()
        val result = client.verstuurNotificatie(
            NotificatieVerzoek(OntvangerIdType.BSN, "123456789", NotificatieKanaal.EMAIL, "Test", "Inhoud")
        )

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("POST", requestSlot.captured.method())
        assertTrue(requestSlot.captured.uri().toString().endsWith("/api/v1/notificaties"))
        assertEquals(notificatieId, result.id)
    }

    @Test
    fun `haalNotificatieStatus verstuurt GET`() {
        mockResponse(200, statusJson())

        val requestSlot = slot<HttpRequest>()
        val result = client.haalNotificatieStatus(notificatieId)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertTrue(requestSlot.captured.uri().toString().endsWith("/api/v1/notificaties/$notificatieId/status"))
        assertEquals(NotificatieStatusWaarde.VERZONDEN, result.status)
    }

    @Test
    fun `200 wordt niet geaccepteerd voor verstuurNotificatie`() {
        mockResponse(200, notificatieJson())

        assertThrows<FbsException> {
            client.verstuurNotificatie(
                NotificatieVerzoek(OntvangerIdType.BSN, "123456789", NotificatieKanaal.EMAIL, "Test", "Inhoud")
            )
        }
    }

    @Test
    fun `fout HTTP status gooit FbsException`() {
        mockResponse(404, """{"type":"about:blank","title":"Not Found","status":404}""",
            contentType = "application/problem+json")

        val exception = assertThrows<FbsException> {
            client.haalNotificatieStatus(notificatieId)
        }
        assertEquals(404, exception.statusCode)
    }

    private fun notificatieJson() = """
        {
            "id": "$notificatieId",
            "ontvangerIdType": "BSN",
            "ontvangerId": "123456789",
            "kanaal": "EMAIL",
            "onderwerp": "Test",
            "inhoud": "Inhoud",
            "status": "AANGEMAAKT",
            "aangemaaktOp": "2025-01-01T00:00:00Z"
        }
    """.trimIndent()

    private fun statusJson() = """
        {
            "notificatieId": "$notificatieId",
            "status": "VERZONDEN",
            "verzondenOp": "2025-01-01T00:01:00Z"
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
