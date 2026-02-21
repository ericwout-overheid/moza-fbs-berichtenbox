package nl.rijksoverheid.moz.notificatie.entity

import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NotificatieEntityTest {

    @Test
    fun `markeerVerzonden slaagt vanuit AANGEMAAKT`() {
        val entity = createEntity()

        entity.markeerVerzonden()

        assertEquals(NotificatieStatusWaarde.VERZONDEN, entity.status)
        assertNotNull(entity.verzondenOp)
    }

    @Test
    fun `markeerVerzonden faalt vanuit VERZONDEN`() {
        val entity = createEntity()
        entity.markeerVerzonden()

        assertThrows<IllegalStateException> {
            entity.markeerVerzonden()
        }
    }

    @Test
    fun `markeerVerzonden faalt vanuit MISLUKT`() {
        val entity = createEntity()
        entity.markeerMislukt("fout")

        assertThrows<IllegalStateException> {
            entity.markeerVerzonden()
        }
    }

    @Test
    fun `markeerMislukt slaagt vanuit AANGEMAAKT`() {
        val entity = createEntity()

        entity.markeerMislukt("SMTP error")

        assertEquals(NotificatieStatusWaarde.MISLUKT, entity.status)
        assertEquals("SMTP error", entity.foutmelding)
    }

    @Test
    fun `markeerMislukt faalt vanuit VERZONDEN`() {
        val entity = createEntity()
        entity.markeerVerzonden()

        assertThrows<IllegalStateException> {
            entity.markeerMislukt("fout")
        }
    }

    @Test
    fun `markeerMislukt faalt vanuit MISLUKT`() {
        val entity = createEntity()
        entity.markeerMislukt("eerste fout")

        assertThrows<IllegalStateException> {
            entity.markeerMislukt("tweede fout")
        }
    }

    private fun createEntity() = NotificatieEntity(
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "999999999",
        kanaal = NotificatieKanaal.EMAIL,
        onderwerp = "Test",
        inhoud = "Test inhoud",
        status = NotificatieStatusWaarde.AANGEMAAKT
    )
}
