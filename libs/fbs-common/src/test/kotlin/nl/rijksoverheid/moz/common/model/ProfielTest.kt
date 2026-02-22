package nl.rijksoverheid.moz.common.model

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ProfielTest {

    @Test
    fun `geldig profiel met email notificaties`() {
        assertDoesNotThrow {
            Profiel(
                ontvangerId = "123456789",
                ontvangerIdType = OntvangerIdType.BSN,
                emailNotificaties = true,
                smsNotificaties = false,
                emailAdres = "test@example.nl"
            )
        }
    }

    @Test
    fun `geldig profiel zonder notificaties`() {
        assertDoesNotThrow {
            Profiel(
                ontvangerId = "123456789",
                ontvangerIdType = OntvangerIdType.BSN,
                emailNotificaties = false,
                smsNotificaties = false
            )
        }
    }

    @Test
    fun `email notificaties zonder emailAdres gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Profiel(
                ontvangerId = "123456789",
                ontvangerIdType = OntvangerIdType.BSN,
                emailNotificaties = true,
                smsNotificaties = false,
                emailAdres = null
            )
        }
    }

    @Test
    fun `email notificaties met leeg emailAdres gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Profiel(
                ontvangerId = "123456789",
                ontvangerIdType = OntvangerIdType.BSN,
                emailNotificaties = true,
                smsNotificaties = false,
                emailAdres = "   "
            )
        }
    }

    @Test
    fun `sms notificaties zonder telefoonnummer gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Profiel(
                ontvangerId = "123456789",
                ontvangerIdType = OntvangerIdType.BSN,
                emailNotificaties = false,
                smsNotificaties = true,
                telefoonnummer = null
            )
        }
    }

    @Test
    fun `geldig profiel met sms en telefoon`() {
        assertDoesNotThrow {
            Profiel(
                ontvangerId = "123456789",
                ontvangerIdType = OntvangerIdType.BSN,
                emailNotificaties = false,
                smsNotificaties = true,
                telefoonnummer = "+31612345678"
            )
        }
    }
}
