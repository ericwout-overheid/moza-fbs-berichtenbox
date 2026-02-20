package nl.fbs.common

/**
 * Federatief Berichtenstelsel - gemeenschappelijke constanten en gedeelde types.
 */
object FbsConstants {
    const val VERSION = "0.1.0-SNAPSHOT"

    /** Maximale lengte van het onderwerp van een bericht */
    const val MAX_ONDERWERP_LENGTH = 500

    /** Standaard paginagrootte voor gepagineerde resultaten */
    const val DEFAULT_PAGE_SIZE = 20

    /** Maximale paginagrootte voor gepagineerde resultaten */
    const val MAX_PAGE_SIZE = 100

    /** Media type voor RFC 9457 Problem Detail responses */
    const val MEDIA_TYPE_PROBLEM_JSON = "application/problem+json"
}
