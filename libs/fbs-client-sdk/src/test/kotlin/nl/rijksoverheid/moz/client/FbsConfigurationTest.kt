package nl.rijksoverheid.moz.client

import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class FbsConfigurationTest {

    @Test
    fun `standaard timeouts zijn correct`() {
        val config = FbsConfiguration(berichtenmagazijnUrl = "http://localhost:8080")

        assertEquals(Duration.ofSeconds(5), config.connectTimeout)
        assertEquals(Duration.ofSeconds(30), config.requestTimeout)
    }

    @Test
    fun `lege berichtenmagazijnUrl gooit exception`() {
        assertThrows<IllegalArgumentException> {
            FbsConfiguration(berichtenmagazijnUrl = "  ")
        }
    }

    @Test
    fun `alle URLs zijn configureerbaar`() {
        val config = FbsConfiguration(
            berichtenmagazijnUrl = "http://host:8080",
            berichtenlijstUrl = "http://host:9081",
            notificatieUrl = "http://host:9082",
            notificatieprofielUrl = "http://host:9083",
            bereikbaarheidUrl = "http://host:9084",
            bearerToken = "test-token"
        )

        assertEquals("http://host:8080", config.berichtenmagazijnUrl)
        assertEquals("http://host:9081", config.berichtenlijstUrl)
        assertEquals("http://host:9082", config.notificatieUrl)
        assertEquals("http://host:9083", config.notificatieprofielUrl)
        assertEquals("http://host:9084", config.bereikbaarheidUrl)
        assertEquals("test-token", config.bearerToken)
    }
}
