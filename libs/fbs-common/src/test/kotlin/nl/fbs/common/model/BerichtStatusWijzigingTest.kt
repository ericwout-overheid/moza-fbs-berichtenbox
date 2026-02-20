package nl.fbs.common.model

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class BerichtStatusWijzigingTest {

    @Test
    fun `GELEZEN is toegestaan`() {
        val wijziging = BerichtStatusWijziging(BerichtStatus.GELEZEN)
        assertEquals(BerichtStatus.GELEZEN, wijziging.status)
    }

    @Test
    fun `GEARCHIVEERD is toegestaan`() {
        val wijziging = BerichtStatusWijziging(BerichtStatus.GEARCHIVEERD)
        assertEquals(BerichtStatus.GEARCHIVEERD, wijziging.status)
    }

    @Test
    fun `NIEUW is niet toegestaan`() {
        assertThrows<IllegalArgumentException> {
            BerichtStatusWijziging(BerichtStatus.NIEUW)
        }
    }

    @Test
    fun `alle toegestane statussen werken`() {
        assertDoesNotThrow { BerichtStatusWijziging(BerichtStatus.GELEZEN) }
        assertDoesNotThrow { BerichtStatusWijziging(BerichtStatus.GEARCHIVEERD) }
    }
}
