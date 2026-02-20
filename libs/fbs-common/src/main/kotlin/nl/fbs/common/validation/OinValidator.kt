package nl.fbs.common.validation

/**
 * Validator voor Organisatie Identificatie Nummers (OIN).
 *
 * Een geldig OIN bestaat uit exact 20 cijfers.
 */
object OinValidator {

    private val OIN_REGEX = Regex("^\\d{20}$")

    /**
     * Controleert of het opgegeven OIN een geldig formaat heeft.
     *
     * @param oin het te valideren OIN
     * @return true als het OIN geldig is
     */
    fun isValid(oin: String): Boolean = OIN_REGEX.matches(oin)

    /**
     * Valideert het opgegeven OIN en gooit een [IllegalArgumentException] als het ongeldig is.
     *
     * @param oin het te valideren OIN
     * @throws IllegalArgumentException als het OIN niet uit exact 20 cijfers bestaat
     */
    fun validate(oin: String) {
        require(isValid(oin)) { "Ongeldig OIN formaat: verwacht 20 cijfers, was '$oin'" }
    }
}
