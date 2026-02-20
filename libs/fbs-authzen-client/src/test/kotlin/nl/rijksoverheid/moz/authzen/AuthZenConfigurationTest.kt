package nl.rijksoverheid.moz.authzen

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthZenConfigurationTest {

    @Test
    fun `evaluationEndpoint wordt correct opgebouwd`() {
        val config = AuthZenConfiguration(pdpUrl = "https://pdp.example.com")
        assertEquals("https://pdp.example.com/access/v1/evaluation", config.evaluationEndpoint)
    }

    @Test
    fun `evaluationEndpoint verwijdert trailing slash`() {
        val config = AuthZenConfiguration(pdpUrl = "https://pdp.example.com/")
        assertEquals("https://pdp.example.com/access/v1/evaluation", config.evaluationEndpoint)
    }

    @Test
    fun `evaluationEndpoint verwijdert meerdere trailing slashes`() {
        val config = AuthZenConfiguration(pdpUrl = "https://pdp.example.com///")
        assertEquals("https://pdp.example.com/access/v1/evaluation", config.evaluationEndpoint)
    }

    @Test
    fun `standaard connectTimeout is 5 seconden`() {
        val config = AuthZenConfiguration(pdpUrl = "https://pdp.example.com")
        assertEquals(Duration.ofSeconds(5), config.connectTimeout)
    }

    @Test
    fun `standaard requestTimeout is 10 seconden`() {
        val config = AuthZenConfiguration(pdpUrl = "https://pdp.example.com")
        assertEquals(Duration.ofSeconds(10), config.requestTimeout)
    }
}
