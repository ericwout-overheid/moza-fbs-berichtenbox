package nl.rijksoverheid.moz.common.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class NotificatieTest {

    @Test
    fun `valide notificatie wordt aangemaakt`() {
        assertDoesNotThrow {
            createNotificatie()
        }
    }

    @Test
    fun `lege ontvangerId wordt afgewezen`() {
        assertThrows<IllegalArgumentException> {
            createNotificatie(ontvangerId = "")
        }
    }

    @Test
    fun `leeg onderwerp wordt afgewezen`() {
        assertThrows<IllegalArgumentException> {
            createNotificatie(onderwerp = "")
        }
    }

    @Test
    fun `lege inhoud wordt afgewezen`() {
        assertThrows<IllegalArgumentException> {
            createNotificatie(inhoud = "")
        }
    }

    @Test
    fun `whitespace-only inhoud wordt afgewezen`() {
        assertThrows<IllegalArgumentException> {
            createNotificatie(inhoud = "   ")
        }
    }

    private fun createNotificatie(
        ontvangerId: String = "999999999",
        onderwerp: String = "Test onderwerp",
        inhoud: String = "Test inhoud"
    ) = Notificatie(
        id = UUID.randomUUID(),
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = ontvangerId,
        kanaal = NotificatieKanaal.EMAIL,
        onderwerp = onderwerp,
        inhoud = inhoud,
        status = NotificatieStatusWaarde.AANGEMAAKT,
        aangemaaktOp = Instant.now()
    )
}
