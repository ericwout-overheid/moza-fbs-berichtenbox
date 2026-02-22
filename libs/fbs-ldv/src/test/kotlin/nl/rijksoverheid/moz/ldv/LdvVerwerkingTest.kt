package nl.rijksoverheid.moz.ldv

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.test.Test

class LdvVerwerkingTest {

    @Test
    fun `geldige verwerking wordt aangemaakt`() {
        assertDoesNotThrow {
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/test"),
                betrokkeneId = "123456789",
                betrokkeneIdType = "BSN",
                operatieNaam = "testOperatie"
            )
        }
    }

    @Test
    fun `lege betrokkeneId gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/test"),
                betrokkeneId = "",
                betrokkeneIdType = "BSN",
                operatieNaam = "testOperatie"
            )
        }
    }

    @Test
    fun `blanco betrokkeneId gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/test"),
                betrokkeneId = "   ",
                betrokkeneIdType = "BSN",
                operatieNaam = "testOperatie"
            )
        }
    }

    @Test
    fun `lege betrokkeneIdType gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/test"),
                betrokkeneId = "123456789",
                betrokkeneIdType = "",
                operatieNaam = "testOperatie"
            )
        }
    }

    @Test
    fun `lege operatieNaam gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/test"),
                betrokkeneId = "123456789",
                betrokkeneIdType = "BSN",
                operatieNaam = ""
            )
        }
    }

    @Test
    fun `blanco operatieNaam gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/test"),
                betrokkeneId = "123456789",
                betrokkeneIdType = "BSN",
                operatieNaam = "   "
            )
        }
    }
}
