package nl.fbs.authzen.model

/**
 * AuthZEN evaluation response van de PDP.
 *
 * @property decision de autorisatiebeslissing (true = toegestaan, false = geweigerd)
 */
data class EvaluationResponse(
    val decision: Boolean
)
