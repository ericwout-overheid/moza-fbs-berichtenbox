package nl.rijksoverheid.moz.client

import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FbsClientTest {

    @Test
    fun `builder zonder berichtenmagazijnUrl gooit exception`() {
        assertThrows<IllegalArgumentException> {
            FbsClient.builder().build()
        }
    }

    @Test
    fun `builder maakt alle sub-clients aan`() {
        val client = FbsClient.builder()
            .berichtenmagazijnUrl("http://localhost:8080")
            .build()

        assertNotNull(client.berichten())
        assertNotNull(client.berichtenlijst())
        assertNotNull(client.notificaties())
        assertNotNull(client.profielen())
        assertNotNull(client.bereikbaarheid())
    }

    @Test
    fun `builder leidt service URLs af van berichtenmagazijn port`() {
        val derived = FbsClient.Builder.deriveUrl("http://localhost:8080", 8081)
        assertEquals("http://localhost:8081", derived)
    }

    @Test
    fun `builder respecteert individueel geconfigureerde URLs`() {
        val client = FbsClient.builder()
            .berichtenmagazijnUrl("http://magazijn:8080")
            .berichtenlijstUrl("http://lijst:9081")
            .notificatieUrl("http://notif:9082")
            .notificatieprofielUrl("http://profiel:9083")
            .bereikbaarheidUrl("http://bereik:9084")
            .build()

        assertNotNull(client.berichten())
    }

    @Test
    fun `deriveUrl verwerkt URL zonder expliciete port`() {
        val derived = FbsClient.Builder.deriveUrl("http://example.com", 8081)
        assertEquals("http://example.com:8081", derived)
    }

    @Test
    fun `deriveUrl verwerkt HTTPS URL`() {
        val derived = FbsClient.Builder.deriveUrl("https://example.com", 8081)
        assertEquals("https://example.com:8081", derived)
    }

    @Test
    fun `deriveUrl gooit bij ongeldige URL`() {
        assertThrows<FbsException> {
            FbsClient.Builder.deriveUrl("not a valid url %%%", 8081)
        }
    }

    @Test
    fun `deriveUrl gooit bij URL zonder scheme`() {
        assertThrows<IllegalArgumentException> {
            FbsClient.Builder.deriveUrl("localhost:8080", 8081)
        }
    }

    @Test
    fun `builder accepteert custom httpClient`() {
        val customClient = HttpClient.newHttpClient()
        val client = FbsClient.builder()
            .berichtenmagazijnUrl("http://localhost:8080")
            .httpClient(customClient)
            .build()

        assertNotNull(client.berichten())
    }
}
