package nl.fbs.cloudevents

/**
 * FBS CloudEvents event types conform NL GOV CloudEvents profiel.
 *
 * Event types gebruiken reverse domain name notation (RDNN).
 */
object FbsEventTypes {
    const val BERICHT_ONTVANGEN = "nl.fbs.bericht.ontvangen"
    const val BERICHT_GELEZEN = "nl.fbs.bericht.gelezen"
    const val BERICHT_VERWIJDERD = "nl.fbs.bericht.verwijderd"
    const val NOTIFICATIE_VERZONDEN = "nl.fbs.notificatie.verzonden"
}
