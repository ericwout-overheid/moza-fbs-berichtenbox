package nl.rijksoverheid.moz.common.model

import nl.rijksoverheid.moz.common.FbsConstants

/**
 * Verzoek om een notificatie te verzenden.
 *
 * @property ontvangerIdType type identificatie van de ontvanger
 * @property ontvangerId identificatie van de ontvanger
 * @property kanaal kanaal waarover de notificatie wordt verzonden
 * @property onderwerp onderwerp van de notificatie
 * @property inhoud inhoud van de notificatie
 */
data class NotificatieVerzoek(
    val ontvangerIdType: OntvangerIdType,
    val ontvangerId: String,
    val kanaal: NotificatieKanaal,
    val onderwerp: String,
    val inhoud: String
) {
    init {
        require(ontvangerId.isNotBlank()) { "ontvangerId mag niet leeg zijn" }
        require(onderwerp.isNotBlank()) { "onderwerp mag niet leeg zijn" }
        require(onderwerp.length <= FbsConstants.MAX_ONDERWERP_LENGTH) {
            "onderwerp mag maximaal ${FbsConstants.MAX_ONDERWERP_LENGTH} tekens bevatten"
        }
        require(inhoud.isNotBlank()) { "inhoud mag niet leeg zijn" }
    }
}
