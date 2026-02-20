package nl.fbs.common.model

/**
 * Verzoek om de status van een bericht te wijzigen.
 *
 * Alleen [BerichtStatus.GELEZEN] en [BerichtStatus.GEARCHIVEERD] zijn toegestaan.
 *
 * @property status de nieuwe status voor het bericht
 */
data class BerichtStatusWijziging(
    val status: BerichtStatus
) {
    init {
        require(status in TOEGESTANE_STATUSSEN) {
            "status moet GELEZEN of GEARCHIVEERD zijn, was: $status"
        }
    }

    companion object {
        private val TOEGESTANE_STATUSSEN = setOf(BerichtStatus.GELEZEN, BerichtStatus.GEARCHIVEERD)
    }
}
