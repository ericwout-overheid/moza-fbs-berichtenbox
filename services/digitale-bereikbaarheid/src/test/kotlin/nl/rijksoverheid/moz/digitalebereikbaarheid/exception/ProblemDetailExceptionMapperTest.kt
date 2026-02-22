package nl.rijksoverheid.moz.digitalebereikbaarheid.exception

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import io.mockk.mockk
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.ProblemDetail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class ProblemDetailExceptionMapperTest {

    private val mapper = ProblemDetailExceptionMapper()

    @Test
    fun `BereikbaarheidNietGevondenException geeft 404`() {
        val exception = BereikbaarheidNietGevondenException("999999999", OntvangerIdType.BSN)

        val response = mapper.toResponse(exception)
        val problem = response.entity as ProblemDetail

        assertEquals(404, response.status)
        assertEquals(FbsConstants.MEDIA_TYPE_PROBLEM_JSON, response.mediaType.toString())
        assertEquals("Niet gevonden", problem.title)
        assertFalse(problem.detail?.contains("999999999") == true, "BSN mag niet in API response lekken")
    }

    @Test
    fun `IllegalArgumentException geeft 400`() {
        val exception = IllegalArgumentException("ongeldig veld")

        val response = mapper.toResponse(exception)
        val problem = response.entity as ProblemDetail

        assertEquals(400, response.status)
        assertEquals("Ongeldig verzoek", problem.title)
        assertEquals("ongeldig veld", problem.detail)
    }

    @Test
    fun `JsonProcessingException geeft 400`() {
        val exception = JsonParseException(mockk<JsonParser>(relaxed = true), "ongeldig")

        val response = mapper.toResponse(exception)
        val problem = response.entity as ProblemDetail

        assertEquals(400, response.status)
        assertEquals("Ongeldig JSON formaat", problem.title)
        assertEquals("Het verzoek bevat ongeldige JSON", problem.detail)
    }

    @Test
    fun `WebApplicationException 500 verbergt details`() {
        val exception = WebApplicationException("gevoelige info", 500)

        val response = mapper.toResponse(exception)
        val problem = response.entity as ProblemDetail

        assertEquals(500, response.status)
        assertEquals("Er is een serverfout opgetreden", problem.detail)
    }

    @Test
    fun `RuntimeException geeft 500 zonder interne details`() {
        val exception = RuntimeException("NullPointerException in dao")

        val response = mapper.toResponse(exception)
        val problem = response.entity as ProblemDetail

        assertEquals(500, response.status)
        assertEquals("Interne serverfout", problem.title)
        assertEquals("Er is een onverwachte fout opgetreden", problem.detail)
    }
}
