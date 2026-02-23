package nl.rijksoverheid.moz.ldv

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Pseudonimiseert betrokkene-identifiers voor het Logboek Dataverwerkingen.
 *
 * De LDV-specificatie BEVEELT AAN (SHOULD) dat `data_subject_id` gepseudonimiseerd wordt.
 * Deze klasse biedt HMAC-SHA256 pseudonimisering: een eenrichtings, deterministische
 * hash die per organisatie uniek is dankzij een organisatie-breed geheim (zout).
 *
 * @property zout organisatie-breed geheim voor pseudonimisering (beheer als secret)
 */
class LdvPseudonimiseerder(private val zout: ByteArray) {

    init {
        require(zout.size >= 32) { "Zout moet minimaal 32 bytes zijn voor voldoende entropie" }
    }

    /**
     * Pseudonimiseert een betrokkene-identifier met HMAC-SHA256.
     *
     * Het resultaat is deterministisch: dezelfde input levert altijd dezelfde output,
     * zodat logregels correleerbaar blijven zonder het originele BSN op te slaan.
     *
     * @param betrokkeneId de te pseudonimiseren identifier (bijv. BSN)
     * @return hex-gecodeerde HMAC-SHA256 hash
     */
    fun pseudonimiseer(betrokkeneId: String): String {
        require(betrokkeneId.isNotBlank()) { "betrokkeneId mag niet leeg zijn" }
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(zout, ALGORITHM))
        return mac.doFinal(betrokkeneId.toByteArray(Charsets.UTF_8)).toHex()
    }

    companion object {
        private const val ALGORITHM = "HmacSHA256"

        /**
         * Maakt een [LdvPseudonimiseerder] aan met het opgegeven zout als UTF-8 string.
         *
         * @param zout organisatie-breed geheim (moet minimaal 32 bytes opleveren na UTF-8 codering)
         */
        fun create(zout: String): LdvPseudonimiseerder {
            require(zout.isNotBlank()) { "Zout mag niet leeg zijn" }
            return LdvPseudonimiseerder(zout.toByteArray(Charsets.UTF_8))
        }
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
