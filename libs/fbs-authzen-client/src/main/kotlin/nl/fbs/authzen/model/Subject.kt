package nl.fbs.authzen.model

/**
 * Subject in een AuthZEN authorization request.
 *
 * @property type type van het subject (bijv. "user", "service")
 * @property id identifier van het subject
 * @property properties aanvullende eigenschappen
 */
data class Subject(
    val type: String,
    val id: String,
    val properties: Map<String, Any> = emptyMap()
)
