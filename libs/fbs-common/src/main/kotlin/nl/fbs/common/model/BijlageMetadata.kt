package nl.fbs.common.model

import java.time.Instant
import java.util.UUID

/**
 * Metadata van een bijlage bij een bericht.
 *
 * @property id unieke identifier van de bijlage
 * @property bestandsnaam originele bestandsnaam
 * @property mediaType MIME type van het bestand
 * @property grootte bestandsgrootte in bytes
 * @property aangemaaktOp tijdstip van aanmaak
 */
data class BijlageMetadata(
    val id: UUID,
    val bestandsnaam: String,
    val mediaType: String,
    val grootte: Long,
    val aangemaaktOp: Instant
) {
    init {
        require(bestandsnaam.isNotBlank()) { "bestandsnaam mag niet leeg zijn" }
        require(mediaType.isNotBlank()) { "mediaType mag niet leeg zijn" }
        require(grootte >= 0) { "grootte moet >= 0 zijn, was: $grootte" }
    }
}
