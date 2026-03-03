package nl.rijksoverheid.moz.berichtenmagazijn.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.ServiceUnavailableException
import nl.rijksoverheid.moz.authzen.AuthZenClient
import nl.rijksoverheid.moz.authzen.AuthZenException
import nl.rijksoverheid.moz.authzen.model.EvaluationRequest
import nl.rijksoverheid.moz.authzen.model.EvaluationResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class AutorisatieServiceTest {

    private val authZenClient = mockk<AuthZenClient>()
    private val berichtId = UUID.randomUUID()
    private val testOin = "00000001234567890000"

    @Test
    fun `controleerToegang grants access when PDP returns decision true`() {
        val service = AutorisatieService(authZenClient, devMode = false)
        every { authZenClient.evaluate(any<EvaluationRequest>()) } returns EvaluationResponse(decision = true)
        assertDoesNotThrow { service.controleerToegang(testOin, "read", berichtId) }
    }

    @Test
    fun `controleerToegang throws ForbiddenException when PDP denies`() {
        val service = AutorisatieService(authZenClient, devMode = false)
        every { authZenClient.evaluate(any<EvaluationRequest>()) } returns EvaluationResponse(decision = false)
        assertThrows<ForbiddenException> { service.controleerToegang(testOin, "read", berichtId) }
    }

    @Test
    fun `controleerToegang throws ServiceUnavailableException when PDP unreachable`() {
        val service = AutorisatieService(authZenClient, devMode = false)
        every { authZenClient.evaluate(any<EvaluationRequest>()) } throws AuthZenException("Connection refused")
        assertThrows<ServiceUnavailableException> { service.controleerToegang(testOin, "read", berichtId) }
    }

    @Test
    fun `controleerToegang skips PDP in dev-mode`() {
        val service = AutorisatieService(authZenClient, devMode = true)
        assertDoesNotThrow { service.controleerToegang(testOin, "read", berichtId) }
        verify(exactly = 0) { authZenClient.evaluate(any()) }
    }

    @Test
    fun `controleerToegang constructs correct EvaluationRequest`() {
        val service = AutorisatieService(authZenClient, devMode = false)
        every { authZenClient.evaluate(any<EvaluationRequest>()) } returns EvaluationResponse(decision = true)

        service.controleerToegang(testOin, "delete", berichtId)

        verify {
            authZenClient.evaluate(match<EvaluationRequest> { req ->
                req.subject.type == "organisatie" &&
                    req.subject.id == testOin &&
                    req.action.name == "delete" &&
                    req.resource.type == "bericht" &&
                    req.resource.id == berichtId.toString()
            })
        }
    }
}
