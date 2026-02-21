package nl.rijksoverheid.moz.common.model

import java.net.URI

/**
 * Problem Detail conform RFC 9457 (application/problem+json).
 *
 * @property type URI die het probleemtype identificeert
 * @property title korte samenvatting van het probleem
 * @property status HTTP-statuscode
 * @property detail menselijk leesbare uitleg van het probleem
 * @property instance URI die de specifieke instantie van het probleem identificeert
 */
data class ProblemDetail(
    val type: URI = URI.create("about:blank"),
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: URI? = null
)
