package nl.fbs.common.model

import nl.fbs.common.FbsConstants
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class BerichtAanmaakVerzoekTest {

    @Test
    fun `geldig verzoek wordt aangemaakt`() {
        assertDoesNotThrow {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "Test onderwerp",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `onderwerp op maximale lengte is geldig`() {
        assertDoesNotThrow {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "a".repeat(FbsConstants.MAX_ONDERWERP_LENGTH),
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `onderwerp boven maximale lengte gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "a".repeat(FbsConstants.MAX_ONDERWERP_LENGTH + 1),
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `leeg onderwerp gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `blanco onderwerp gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "   ",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `lege ontvangerId gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "",
                onderwerp = "Test onderwerp",
                inhoud = "Test inhoud"
            )
        }
    }

    @Test
    fun `lege inhoud gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "Test onderwerp",
                inhoud = ""
            )
        }
    }

    @Test
    fun `blanco inhoud gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "123456789",
                onderwerp = "Test onderwerp",
                inhoud = "   "
            )
        }
    }
}
