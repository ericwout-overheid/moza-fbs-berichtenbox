package nl.rijksoverheid.moz.cloudevents

import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FbsSourceUrnTest {

    private val geldigOin = "00000001234567890123"

    @Test
    fun `create maakt geldige URN aan`() {
        val urn = FbsSourceUrn.create(geldigOin, "berichtenmagazijn")
        assertEquals(URI.create("urn:nld:oin:${geldigOin}:systeem:berichtenmagazijn"), urn)
    }

    @Test
    fun `create met koppelteken in systeemnaam`() {
        val urn = FbsSourceUrn.create(geldigOin, "berichten-magazijn")
        assertEquals(URI.create("urn:nld:oin:${geldigOin}:systeem:berichten-magazijn"), urn)
    }

    @Test
    fun `create met ongeldig OIN gooit exception`() {
        assertThrows<IllegalArgumentException> {
            FbsSourceUrn.create("ongeldig", "systeem")
        }
    }

    @Test
    fun `create met lege systeemnaam gooit exception`() {
        assertThrows<IllegalArgumentException> {
            FbsSourceUrn.create(geldigOin, "")
        }
    }

    @Test
    fun `create met ongeldige tekens in systeemnaam gooit exception`() {
        assertThrows<IllegalArgumentException> {
            FbsSourceUrn.create(geldigOin, "systeem met spaties")
        }
    }

    @Test
    fun `isValid voor geldige URN`() {
        val urn = FbsSourceUrn.create(geldigOin, "test")
        assertTrue(FbsSourceUrn.isValid(urn))
    }

    @Test
    fun `isValid voor ongeldige URI`() {
        assertFalse(FbsSourceUrn.isValid(URI.create("https://example.com")))
    }

    @Test
    fun `extractOin uit geldige URN`() {
        val urn = FbsSourceUrn.create(geldigOin, "test")
        assertEquals(geldigOin, FbsSourceUrn.extractOin(urn))
    }

    @Test
    fun `extractOin uit ongeldige URN gooit exception`() {
        assertThrows<IllegalArgumentException> {
            FbsSourceUrn.extractOin(URI.create("urn:invalid"))
        }
    }
}
