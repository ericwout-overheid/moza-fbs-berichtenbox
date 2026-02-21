package nl.rijksoverheid.moz.notificatie.service

import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class SmsNotificatieVerzenderTest {

    @Test
    fun `stub modus verzendt zonder fout`() {
        val verzender = SmsNotificatieVerzender(stubModus = true)

        assertDoesNotThrow {
            verzender.verzend("+31612345678", "Test onderwerp", "Test inhoud")
        }
    }

    @Test
    fun `productie modus gooit UnsupportedOperationException`() {
        val verzender = SmsNotificatieVerzender(stubModus = false)

        assertThrows<UnsupportedOperationException> {
            verzender.verzend("+31612345678", "Test onderwerp", "Test inhoud")
        }
    }

    @Test
    fun `kanaal is SMS`() {
        val verzender = SmsNotificatieVerzender(stubModus = true)

        assertEquals(NotificatieKanaal.SMS, verzender.kanaal)
    }
}
