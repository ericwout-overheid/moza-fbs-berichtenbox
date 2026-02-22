package nl.rijksoverheid.moz.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BerichtenlijstClientTest {

    private val httpClient = mockk<HttpClient>()
    private val http = FbsHttpSupport.create(
        bearerToken = "test-token",
        connectTimeout = Duration.ofSeconds(5),
        requestTimeout = Duration.ofSeconds(30),
        httpClient = httpClient
    )
    private val client = BerichtenlijstClient("http://localhost:8081", http)

    @Test
    fun `haalBerichtenlijst verstuurt GET met verplichte parameters`() {
        mockResponse(200, pageJson())

        val requestSlot = slot<HttpRequest>()
        val result = client.haalBerichtenlijst(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "123456789"
        )

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        val uri = requestSlot.captured.uri().toString()
        assertTrue(uri.contains("/api/v1/berichtenlijst?"))
        assertTrue(uri.contains("ontvangerIdType=BSN"))
        assertTrue(uri.contains("ontvangerId=123456789"))
        assertEquals(1, result.resultaten.size)
    }

    @Test
    fun `zoekBerichten verstuurt GET met zoekterm`() {
        mockResponse(200, pageJson())

        val requestSlot = slot<HttpRequest>()
        val result = client.zoekBerichten(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "123456789",
            zoekterm = "belasting"
        )

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        val uri = requestSlot.captured.uri().toString()
        assertTrue(uri.contains("/api/v1/berichtenlijst/zoek?"))
        assertTrue(uri.contains("zoekterm=belasting"))
        assertEquals(1, result.resultaten.size)
    }

    @Test
    fun `paginering parameters worden meegezonden`() {
        mockResponse(200, pageJson())

        val requestSlot = slot<HttpRequest>()
        client.haalBerichtenlijst(
            ontvangerIdType = OntvangerIdType.RSIN,
            ontvangerId = "999999999",
            page = 3,
            pageSize = 50
        )

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        val uri = requestSlot.captured.uri().toString()
        assertTrue(uri.contains("page=3"))
        assertTrue(uri.contains("pageSize=50"))
    }

    @Test
    fun `fout HTTP status gooit FbsException`() {
        mockResponse(500, """{"type":"about:blank","title":"Server Error","status":500}""",
            contentType = "application/problem+json")

        val exception = assertThrows<FbsException> {
            client.haalBerichtenlijst(OntvangerIdType.BSN, "123456789")
        }
        assertEquals(500, exception.statusCode)
    }

    private fun pageJson() = """
        {
            "results": [{
                "berichtId": "00000000-0000-0000-0000-000000000001",
                "afzenderOin": "00000001234567890000",
                "afzenderNaam": "Belastingdienst",
                "onderwerp": "Test bericht",
                "status": "NIEUW",
                "aangemaaktOp": "2025-01-01T00:00:00Z",
                "magazijnUrl": "http://localhost:8080"
            }],
            "page": 1,
            "pageSize": 20,
            "totalPages": 1,
            "totalElements": 1
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
