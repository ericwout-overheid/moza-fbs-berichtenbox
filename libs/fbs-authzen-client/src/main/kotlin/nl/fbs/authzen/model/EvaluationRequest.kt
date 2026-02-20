package nl.fbs.authzen.model

/**
 * AuthZEN evaluation request conform de AuthZEN specificatie.
 *
 * @property subject het subject dat de actie wil uitvoeren
 * @property resource de resource waarop de actie wordt uitgevoerd
 * @property action de uit te voeren actie
 * @property context aanvullende context voor de evaluatie
 */
data class EvaluationRequest(
    val subject: Subject,
    val resource: Resource,
    val action: Action,
    val context: Map<String, Any> = emptyMap()
)
