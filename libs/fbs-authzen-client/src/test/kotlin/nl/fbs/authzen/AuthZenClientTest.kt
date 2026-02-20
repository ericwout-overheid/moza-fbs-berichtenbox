package nl.fbs.authzen

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import nl.fbs.authzen.model.Action
import nl.fbs.authzen.model.EvaluationRequest
import nl.fbs.authzen.model.Resource
import nl.fbs.authzen.model.Subject
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthZenClientTest {

    private val httpClient = mockk<HttpClient>()
    private val configuration = AuthZenConfiguration(pdpUrl = "https://pdp.example.com")
    private val client = AuthZenClient(configuration, httpClient)

    @Test
    fun `positieve beslissing wordt correct verwerkt`() {
        mockResponse(200, """{"decision": true}""")

        val response = client.evaluate(createRequest())
        assertTrue(response.decision)
    }

    @Test
    fun `negatieve beslissing wordt correct verwerkt`() {
        mockResponse(200, """{"decision": false}""")

        val response = client.evaluate(createRequest())
        assertFalse(response.decision)
    }

    @Test
    fun `traceparent header wordt meegezonden`() {
        mockResponse(200, """{"decision": true}""")

        val requestSlot = slot<HttpRequest>()
        client.evaluate(createRequest(), traceparent = "00-trace-span-01")

        verify { httpClient.send(capture(requestSlot), any<HttpResponse.BodyHandler<String>>()) }
        assertEquals(
            "00-trace-span-01",
            requestSlot.captured.headers().firstValue("traceparent").get()
        )
    }

    @Test
    fun `ongeldige JSON response gooit AuthZenException`() {
        mockResponse(200, "dit is geen geldige json")

        val exception = assertThrows<AuthZenException> {
            client.evaluate(createRequest())
        }
        assertEquals(200, exception.statusCode)
        assertNotNull(exception.cause)
    }

    @Test
    fun `non-200 response gooit AuthZenException`() {
        mockResponse(403, """{"error": "forbidden"}""")

        val exception = assertThrows<AuthZenException> {
            client.evaluate(createRequest())
        }
        assertTrue(exception.statusCode == 403)
    }

    @Test
    fun `verbindingsfout gooit AuthZenException`() {
        every {
            httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())
        } throws java.net.ConnectException("Connection refused")

        assertThrows<AuthZenException> {
            client.evaluate(createRequest())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mockResponse(statusCode: Int, body: String) {
        val response = mockk<HttpResponse<String>>()
        every { response.statusCode() } returns statusCode
        every { response.body() } returns body
        every {
            httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())
        } returns response as HttpResponse<String>
    }

    private fun createRequest() = EvaluationRequest(
        subject = Subject(type = "user", id = "user-123"),
        resource = Resource(type = "bericht", id = "bericht-456"),
        action = Action(name = "read")
    )
}
