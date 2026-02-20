package nl.fbs.common.model

import java.time.Instant
import java.util.UUID

/**
 * Notificatie zoals opgeslagen in het systeem.
 *
 * @property id unieke identifier van de notificatie
 * @property kanaal kanaal waarover de notificatie is verzonden
 * @property onderwerp onderwerp van de notificatie
 * @property inhoud inhoud van de notificatie
 * @property status huidige status van de notificatie
 * @property aangemaaktOp tijdstip van aanmaak
 * @property verzondenOp tijdstip van verzending (null indien nog niet verzonden)
 * @property afgeleverdOp tijdstip van aflevering (null indien nog niet afgeleverd)
 */
data class Notificatie(
    val id: UUID,
    val kanaal: NotificatieKanaal,
    val onderwerp: String,
    val inhoud: String,
    val status: NotificatieStatusWaarde,
    val aangemaaktOp: Instant,
    val verzondenOp: Instant? = null,
    val afgeleverdOp: Instant? = null
)
