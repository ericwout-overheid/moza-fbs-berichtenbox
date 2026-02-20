package nl.fbs.cloudevents

import nl.fbs.common.validation.OinValidator
import java.net.URI

/**
 * Bouwt en parseert URN-bronidentifiers voor FBS CloudEvents.
 *
 * Formaat: `urn:nld:oin:<OIN>:systeem:<systeemnaam>`
 */
object FbsSourceUrn {

    private const val URN_PREFIX = "urn:nld:oin:"
    private const val SYSTEEM_INFIX = ":systeem:"
    private val URN_PATTERN = Regex("^urn:nld:oin:(\\d{20}):systeem:([a-zA-Z0-9-]+)$")

    /**
     * Maakt een URN source identifier aan.
     *
     * @param oin Organisatie Identificatie Nummer (20 cijfers)
     * @param systeemNaam naam van het systeem (alfanumeriek en koppeltekens)
     * @return de URN als [URI]
     * @throws IllegalArgumentException bij ongeldig OIN of systeemnaam
     */
    fun create(oin: String, systeemNaam: String): URI {
        OinValidator.validate(oin)
        require(systeemNaam.isNotBlank()) { "systeemNaam mag niet leeg zijn" }
        require(systeemNaam.matches(Regex("^[a-zA-Z0-9-]+$"))) {
            "systeemNaam mag alleen alfanumerieke tekens en koppeltekens bevatten"
        }
        return URI.create("$URN_PREFIX$oin$SYSTEEM_INFIX$systeemNaam")
    }

    /**
     * Valideert of de URI een geldige FBS source URN is.
     *
     * @param uri de te valideren URI
     * @return true als de URI een geldige FBS source URN is
     */
    fun isValid(uri: URI): Boolean = URN_PATTERN.matches(uri.toString())

    /**
     * Extraheert het OIN uit een FBS source URN.
     *
     * @param uri de FBS source URN
     * @return het OIN
     * @throws IllegalArgumentException als de URI geen geldige FBS source URN is
     */
    fun extractOin(uri: URI): String {
        val match = URN_PATTERN.matchEntire(uri.toString())
            ?: throw IllegalArgumentException("Geen geldige FBS source URN: $uri")
        return match.groupValues[1]
    }
}
