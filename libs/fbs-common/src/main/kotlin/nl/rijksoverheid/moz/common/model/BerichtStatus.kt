package nl.rijksoverheid.moz.common.model

/**
 * Status van een bericht in het berichtenmagazijn.
 */
enum class BerichtStatus {
    /** Bericht is nieuw en ongelezen */
    NIEUW,

    /** Bericht is gelezen door de ontvanger */
    GELEZEN,

    /** Bericht is gearchiveerd */
    GEARCHIVEERD
}
