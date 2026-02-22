package nl.rijksoverheid.moz.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.rijksoverheid.moz.common.model.*
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BerichtenClientTest {

    private val httpClient = mockk<HttpClient>()
    private val http = FbsHttpSupport.create(
        bearerToken = "test-token",
        connectTimeout = Duration.ofSeconds(5),
        requestTimeout = Duration.ofSeconds(30),
        httpClient = httpClient
    )
    private val client = BerichtenClient("http://localhost:8080", http)

    private val berichtId = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @Test
    fun `maakBericht verstuurt POST en retourneert bericht`() {
        val responseJson = berichtJson(berichtId)
        mockResponse(201, responseJson)

        val requestSlot = slot<HttpRequest>()
        val result = client.maakBericht(
            BerichtAanmaakVerzoek(OntvangerIdType.BSN, "123456789", "Test", "Inhoud")
        )

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("POST", requestSlot.captured.method())
        assertTrue(requestSlot.captured.uri().toString().endsWith("/api/v1/berichten"))
        assertEquals(berichtId, result.id)
    }

    @Test
    fun `lijstBerichten verstuurt GET met query parameters`() {
        val pageJson = pageJson(berichtJson(berichtId))
        mockResponse(200, pageJson)

        val requestSlot = slot<HttpRequest>()
        val result = client.lijstBerichten(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "123456789",
            status = BerichtStatus.NIEUW,
            page = 2,
            pageSize = 10
        )

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        val uri = requestSlot.captured.uri().toString()
        assertTrue(uri.contains("ontvangerIdType=BSN"))
        assertTrue(uri.contains("ontvangerId=123456789"))
        assertTrue(uri.contains("status=NIEUW"))
        assertTrue(uri.contains("page=2"))
        assertTrue(uri.contains("pageSize=10"))
        assertEquals(1, result.resultaten.size)
    }

    @Test
    fun `haalBericht verstuurt GET met berichtId`() {
        mockResponse(200, berichtJson(berichtId))

        val requestSlot = slot<HttpRequest>()
        val result = client.haalBericht(berichtId)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("GET", requestSlot.captured.method())
        assertTrue(requestSlot.captured.uri().toString().endsWith("/api/v1/berichten/$berichtId"))
        assertEquals(berichtId, result.id)
    }

    @Test
    fun `werkBerichtBij verstuurt PATCH`() {
        mockResponse(200, berichtJson(berichtId, status = "GELEZEN"))

        val requestSlot = slot<HttpRequest>()
        val result = client.werkBerichtBij(berichtId, BerichtStatusWijziging(BerichtStatus.GELEZEN))

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("PATCH", requestSlot.captured.method())
        assertEquals(BerichtStatus.GELEZEN, result.status)
    }

    @Test
    fun `verwijderBericht verstuurt DELETE`() {
        mockResponse(204, "")

        val requestSlot = slot<HttpRequest>()
        client.verwijderBericht(berichtId)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("DELETE", requestSlot.captured.method())
    }

    @Test
    fun `lijstBijlagen retourneert lijst van metadata`() {
        mockResponse(200, """[${bijlageJson()}]""")

        val result = client.lijstBijlagen(berichtId)

        assertEquals(1, result.size)
        assertEquals("test.pdf", result[0].bestandsnaam)
    }

    @Test
    fun `uploadBijlage verstuurt multipart POST`() {
        mockResponse(201, bijlageJson())

        val requestSlot = slot<HttpRequest>()
        val inhoud = ByteArrayInputStream("test content".toByteArray())
        val result = client.uploadBijlage(berichtId, "test.pdf", "application/pdf", inhoud)

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals("POST", requestSlot.captured.method())
        assertTrue(
            requestSlot.captured.headers().firstValue("Content-Type").get()
                .startsWith("multipart/form-data")
        )
        assertEquals("test.pdf", result.bestandsnaam)
    }

    @Test
    fun `traceparent header wordt doorgegeven`() {
        mockResponse(200, berichtJson(berichtId))

        val requestSlot = slot<HttpRequest>()
        client.haalBericht(berichtId, traceparent = "00-trace-span-01")

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals(
            "00-trace-span-01",
            requestSlot.captured.headers().firstValue("traceparent").get()
        )
    }

    @Test
    fun `uploadBijlage met falende InputStream gooit FbsException`() {
        val failingStream = object : InputStream() {
            override fun read(): Int = throw IOException("Disk read error")
        }

        val exception = assertThrows<FbsException> {
            client.uploadBijlage(berichtId, "test.pdf", "application/pdf", failingStream)
        }
        assertTrue(exception.message!!.contains("test.pdf"))
        assertTrue(exception.cause is IOException)
    }

    @Test
    fun `fout HTTP status gooit FbsException`() {
        mockResponse(404, """{"type":"about:blank","title":"Not Found","status":404}""",
            contentType = "application/problem+json")

        val exception = assertThrows<FbsException> {
            client.haalBericht(berichtId)
        }
        assertEquals(404, exception.statusCode)
        assertNotNull(exception.problemDetail)
    }

    private fun berichtJson(id: UUID, status: String = "NIEUW") = """
        {
            "id": "$id",
            "afzenderOin": "00000001234567890000",
            "ontvangerIdType": "BSN",
            "ontvangerId": "123456789",
            "onderwerp": "Test bericht",
            "inhoud": "Dit is een test.",
            "status": "$status",
            "aangemaaktOp": "2025-01-01T00:00:00Z",
            "bijlagen": []
        }
    """.trimIndent()

    private fun pageJson(itemJson: String) = """
        {
            "results": [$itemJson],
            "page": 1,
            "pageSize": 20,
            "totalPages": 1,
            "totalElements": 1
        }
    """.trimIndent()

    private fun bijlageJson() = """
        {
            "id": "00000000-0000-0000-0000-000000000099",
            "bestandsnaam": "test.pdf",
            "mediaType": "application/pdf",
            "grootte": 1024,
            "aangemaaktOp": "2025-01-01T00:00:00Z"
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
