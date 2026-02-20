package nl.rijksoverheid.moz.authzen.model

/**
 * Resource in een AuthZEN authorization request.
 *
 * @property type type van de resource (bijv. "bericht", "notificatie")
 * @property id identifier van de resource
 * @property properties aanvullende eigenschappen
 */
data class Resource(
    val type: String,
    val id: String,
    val properties: Map<String, Any> = emptyMap()
)
