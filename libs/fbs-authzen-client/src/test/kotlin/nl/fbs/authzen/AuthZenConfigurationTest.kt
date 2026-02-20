package nl.fbs.authzen

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
}
