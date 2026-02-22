package nl.rijksoverheid.moz.common.util

import kotlin.test.Test
import kotlin.test.assertEquals

class PiiMaskerTest {

    @Test
    fun `maskeert BSN correct`() {
        assertEquals("***6789", PiiMasker.mask("123456789"))
    }

    @Test
    fun `maskeert korte waarde volledig`() {
        assertEquals("***", PiiMasker.mask("1234"))
    }

    @Test
    fun `maskeert lege string`() {
        assertEquals("***", PiiMasker.mask(""))
    }

    @Test
    fun `maskeert exact 5 tekens`() {
        assertEquals("***2345", PiiMasker.mask("12345"))
    }

    @Test
    fun `maskeert lang OIN`() {
        assertEquals("***0000", PiiMasker.mask("00000001234567890000"))
    }
}
