package nl.fbs.common.model

/**
 * Verzoek om een notificatie te verzenden.
 *
 * @property kanaal kanaal waarover de notificatie wordt verzonden
 * @property onderwerp onderwerp van de notificatie (max 200 tekens)
 * @property inhoud inhoud van de notificatie
 */
data class NotificatieVerzoek(
    val kanaal: NotificatieKanaal,
    val onderwerp: String,
    val inhoud: String
) {
    init {
        require(onderwerp.isNotBlank()) { "onderwerp mag niet leeg zijn" }
        require(onderwerp.length <= MAX_ONDERWERP_LENGTH) {
            "onderwerp mag maximaal $MAX_ONDERWERP_LENGTH tekens bevatten"
        }
        require(inhoud.isNotBlank()) { "inhoud mag niet leeg zijn" }
    }

    companion object {
        private const val MAX_ONDERWERP_LENGTH = 200
    }
}
