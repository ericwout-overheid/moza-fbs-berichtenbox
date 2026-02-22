package nl.rijksoverheid.moz.common.model

/**
 * Status van een notificatie in het verzendproces.
 */
enum class NotificatieStatusWaarde {
    /** Notificatie is aangemaakt maar nog niet verzonden */
    AANGEMAAKT,

    /** Notificatie is verzonden naar het kanaal */
    VERZONDEN,

    /** Notificatie is afgeleverd bij de ontvanger (nog niet in gebruik) */
    AFGELEVERD,

    /** Verzending van de notificatie is mislukt */
    MISLUKT,

    /** Verzending is definitief mislukt na maximaal aantal pogingen */
    DEFINITIEF_MISLUKT
}
