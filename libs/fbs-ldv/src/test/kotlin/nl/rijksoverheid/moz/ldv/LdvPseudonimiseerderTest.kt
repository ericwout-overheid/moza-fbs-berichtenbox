package nl.rijksoverheid.moz.ldv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LdvPseudonimiseerderTest {

    private val zout = "test-zout-minimaal-32-tekens-lang!"
    private val pseudonimiseerder = LdvPseudonimiseerder.create(zout)

    @Test
    fun `pseudonimiseer produces deterministic output`() {
        val result1 = pseudonimiseerder.pseudonimiseer("999999999")
        val result2 = pseudonimiseerder.pseudonimiseer("999999999")
        assertEquals(result1, result2)
    }

    @Test
    fun `pseudonimiseer produces different output for different inputs`() {
        val result1 = pseudonimiseerder.pseudonimiseer("999999999")
        val result2 = pseudonimiseerder.pseudonimiseer("888888888")
        assertNotEquals(result1, result2)
    }

    @Test
    fun `pseudonimiseer produces valid hex string of 64 characters`() {
        val result = pseudonimiseerder.pseudonimiseer("999999999")
        assertEquals(64, result.length)
        assertTrue(result.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `pseudonimiseer rejects blank input`() {
        assertFailsWith<IllegalArgumentException> {
            pseudonimiseerder.pseudonimiseer("")
        }
        assertFailsWith<IllegalArgumentException> {
            pseudonimiseerder.pseudonimiseer("   ")
        }
    }

    @Test
    fun `constructor rejects salt shorter than 32 bytes`() {
        assertFailsWith<IllegalArgumentException> {
            LdvPseudonimiseerder(ByteArray(31))
        }
    }

    @Test
    fun `constructor accepts salt of exactly 32 bytes`() {
        LdvPseudonimiseerder(ByteArray(32) { 1 })
    }

    @Test
    fun `create factory rejects blank salt`() {
        assertFailsWith<IllegalArgumentException> {
            LdvPseudonimiseerder.create("")
        }
    }

    @Test
    fun `different salts produce different outputs for same input`() {
        val p1 = LdvPseudonimiseerder.create("aaaa-zout-minimaal-32-tekens-lang!")
        val p2 = LdvPseudonimiseerder.create("bbbb-zout-minimaal-32-tekens-lang!")
        assertNotEquals(
            p1.pseudonimiseer("999999999"),
            p2.pseudonimiseer("999999999")
        )
    }
}
