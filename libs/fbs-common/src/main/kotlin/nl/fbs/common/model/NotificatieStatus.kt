package nl.fbs.common.model

import java.time.Instant
import java.util.UUID

/**
 * Afleveringsstatus van een notificatie.
 *
 * @property notificatieId identifier van de notificatie
 * @property status huidige status van de notificatie
 * @property verzondenOp tijdstip van verzending (null indien nog niet verzonden)
 * @property afgeleverdOp tijdstip van aflevering (null indien nog niet afgeleverd)
 * @property foutmelding foutmelding bij status [NotificatieStatusWaarde.MISLUKT]
 */
data class NotificatieStatus(
    val notificatieId: UUID,
    val status: NotificatieStatusWaarde,
    val verzondenOp: Instant? = null,
    val afgeleverdOp: Instant? = null,
    val foutmelding: String? = null
) {
    init {
        if (status == NotificatieStatusWaarde.MISLUKT) {
            require(!foutmelding.isNullOrBlank()) {
                "foutmelding is verplicht bij status MISLUKT"
            }
        } else {
            require(foutmelding == null) {
                "foutmelding mag alleen gezet worden bij status MISLUKT"
            }
        }
    }
}
