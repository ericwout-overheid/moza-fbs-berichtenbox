package nl.rijksoverheid.moz.cloudevents

/**
 * FBS CloudEvents event types conform NL GOV CloudEvents profiel.
 *
 * Event types gebruiken reverse domain name notation (RDNN).
 */
object FbsEventTypes {
    const val BERICHT_ONTVANGEN = "nl.rijksoverheid.moz.bericht.ontvangen"
    const val BERICHT_GELEZEN = "nl.rijksoverheid.moz.bericht.gelezen"
    const val BERICHT_VERWIJDERD = "nl.rijksoverheid.moz.bericht.verwijderd"
}
