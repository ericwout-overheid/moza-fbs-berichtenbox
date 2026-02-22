package nl.rijksoverheid.moz.common.model

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class BijlageMetadataTest {

    @Test
    fun `geldige bijlage metadata wordt aangemaakt`() {
        assertDoesNotThrow {
            BijlageMetadata(
                id = UUID.randomUUID(),
                bestandsnaam = "document.pdf",
                mediaType = "application/pdf",
                grootte = 1024,
                aangemaaktOp = Instant.now()
            )
        }
    }

    @Test
    fun `lege bestandsnaam gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BijlageMetadata(
                id = UUID.randomUUID(),
                bestandsnaam = "",
                mediaType = "application/pdf",
                grootte = 1024,
                aangemaaktOp = Instant.now()
            )
        }
    }

    @Test
    fun `blanco bestandsnaam gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BijlageMetadata(
                id = UUID.randomUUID(),
                bestandsnaam = "   ",
                mediaType = "application/pdf",
                grootte = 1024,
                aangemaaktOp = Instant.now()
            )
        }
    }

    @Test
    fun `leeg mediaType gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BijlageMetadata(
                id = UUID.randomUUID(),
                bestandsnaam = "document.pdf",
                mediaType = "",
                grootte = 1024,
                aangemaaktOp = Instant.now()
            )
        }
    }

    @Test
    fun `negatieve grootte gooit exception`() {
        assertThrows<IllegalArgumentException> {
            BijlageMetadata(
                id = UUID.randomUUID(),
                bestandsnaam = "document.pdf",
                mediaType = "application/pdf",
                grootte = -1,
                aangemaaktOp = Instant.now()
            )
        }
    }

    @Test
    fun `grootte nul is geldig`() {
        assertDoesNotThrow {
            BijlageMetadata(
                id = UUID.randomUUID(),
                bestandsnaam = "leeg.txt",
                mediaType = "text/plain",
                grootte = 0,
                aangemaaktOp = Instant.now()
            )
        }
    }
}
