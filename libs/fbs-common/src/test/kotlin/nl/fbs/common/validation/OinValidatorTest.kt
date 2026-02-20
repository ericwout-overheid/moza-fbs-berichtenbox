package nl.fbs.common.validation

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OinValidatorTest {

    @Test
    fun `geldig OIN van 20 cijfers`() {
        assertTrue(OinValidator.isValid("00000001234567890123"))
    }

    @Test
    fun `validate gooit geen exception voor geldig OIN`() {
        assertDoesNotThrow { OinValidator.validate("00000001234567890123") }
    }

    @Test
    fun `te kort OIN is ongeldig`() {
        assertFalse(OinValidator.isValid("1234567890"))
    }

    @Test
    fun `te lang OIN is ongeldig`() {
        assertFalse(OinValidator.isValid("123456789012345678901"))
    }

    @Test
    fun `OIN met letters is ongeldig`() {
        assertFalse(OinValidator.isValid("0000000123456789ABCD"))
    }

    @Test
    fun `leeg OIN is ongeldig`() {
        assertFalse(OinValidator.isValid(""))
    }

    @Test
    fun `OIN met spaties is ongeldig`() {
        assertFalse(OinValidator.isValid("0000 0001 2345 6789 0123"))
    }

    @Test
    fun `validate gooit exception voor ongeldig OIN`() {
        val exception = assertThrows<IllegalArgumentException> {
            OinValidator.validate("ongeldig")
        }
        assertTrue(exception.message!!.contains("Ongeldig OIN formaat"))
    }

    @Test
    fun `OIN van exact 20 nullen is geldig`() {
        assertTrue(OinValidator.isValid("0".repeat(20)))
    }

    @Test
    fun `OIN van exact 20 negens is geldig`() {
        assertTrue(OinValidator.isValid("9".repeat(20)))
    }
}
