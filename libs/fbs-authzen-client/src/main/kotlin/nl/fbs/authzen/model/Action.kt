package nl.fbs.authzen.model

/**
 * Action in een AuthZEN authorization request.
 *
 * @property name naam van de actie (bijv. "read", "write", "delete")
 * @property properties aanvullende eigenschappen
 */
data class Action(
    val name: String,
    val properties: Map<String, Any> = emptyMap()
)
