package nl.rijksoverheid.moz.common.model

import nl.rijksoverheid.moz.common.FbsConstants
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class NotificatieVerzoekTest {

    @Test
    fun `geldig verzoek wordt aangemaakt`() {
        assertDoesNotThrow {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                kanaal = NotificatieKanaal.EMAIL,
                onderwerp = "Test onderwerp",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `lege ontvangerId gooit exception`() {
        assertThrows<IllegalArgumentException> {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "",
                kanaal = NotificatieKanaal.EMAIL,
                onderwerp = "Test onderwerp",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `leeg onderwerp gooit exception`() {
        assertThrows<IllegalArgumentException> {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                kanaal = NotificatieKanaal.EMAIL,
                onderwerp = "",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `onderwerp op maximale lengte is geldig`() {
        assertDoesNotThrow {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                kanaal = NotificatieKanaal.EMAIL,
                onderwerp = "a".repeat(FbsConstants.MAX_ONDERWERP_LENGTH),
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `onderwerp boven maximale lengte gooit exception`() {
        assertThrows<IllegalArgumentException> {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                kanaal = NotificatieKanaal.EMAIL,
                onderwerp = "a".repeat(FbsConstants.MAX_ONDERWERP_LENGTH + 1),
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `lege inhoud gooit exception`() {
        assertThrows<IllegalArgumentException> {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                kanaal = NotificatieKanaal.EMAIL,
                onderwerp = "Test onderwerp",
                inhoud = ""
            )
        }
    }

    @Test
    fun `blanco inhoud gooit exception`() {
        assertThrows<IllegalArgumentException> {
            NotificatieVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                kanaal = NotificatieKanaal.SMS,
                onderwerp = "Test onderwerp",
                inhoud = "   "
            )
        }
    }
}
