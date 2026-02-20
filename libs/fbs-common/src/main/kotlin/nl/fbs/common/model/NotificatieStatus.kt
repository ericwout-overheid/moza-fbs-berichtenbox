package nl.fbs.common.model

import java.util.UUID

/**
 * Statusupdate van een notificatie.
 *
 * @property notificatieId identifier van de notificatie
 * @property status nieuwe status van de notificatie
 * @property foutmelding foutmelding bij status [NotificatieStatusWaarde.MISLUKT]
 */
data class NotificatieStatus(
    val notificatieId: UUID,
    val status: NotificatieStatusWaarde,
    val foutmelding: String? = null
)
