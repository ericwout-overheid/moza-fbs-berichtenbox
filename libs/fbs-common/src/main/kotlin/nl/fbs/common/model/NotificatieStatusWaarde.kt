package nl.fbs.common.model

/**
 * Status van een notificatie in het verzendproces.
 */
enum class NotificatieStatusWaarde {
    /** Notificatie is aangemaakt maar nog niet verzonden */
    AANGEMAAKT,

    /** Notificatie is verzonden naar het kanaal */
    VERZONDEN,

    /** Notificatie is afgeleverd bij de ontvanger */
    AFGELEVERD,

    /** Verzending van de notificatie is mislukt */
    MISLUKT
}
