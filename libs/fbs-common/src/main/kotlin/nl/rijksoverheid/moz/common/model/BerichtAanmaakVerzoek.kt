package nl.rijksoverheid.moz.common.model

import nl.rijksoverheid.moz.common.FbsConstants

/**
 * Verzoek om een nieuw bericht aan te maken in het berichtenmagazijn.
 *
 * @property ontvangerIdType type identifier van de ontvanger
 * @property ontvangerId identifier van de ontvanger
 * @property onderwerp onderwerp van het bericht (max [FbsConstants.MAX_ONDERWERP_LENGTH] tekens)
 * @property inhoud inhoud van het bericht
 */
data class BerichtAanmaakVerzoek(
    val ontvangerIdType: OntvangerIdType,
    val ontvangerId: String,
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
