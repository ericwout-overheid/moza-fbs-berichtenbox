package nl.rijksoverheid.moz.common.util

/**
 * Maskeert persoonsgebonden identifiers (BSN, etc.) voor veilige logging.
 */
object PiiMasker {
    fun mask(value: String): String = if (value.length > 4) "***${value.takeLast(4)}" else "***"
}
