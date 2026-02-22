package nl.rijksoverheid.moz.client

import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import java.net.URI

/**
 * Client voor het Notificatieprofiel API.
 *
 * @see <a href="openapi/notificatieprofiel-v1.yaml">OpenAPI spec</a>
 */
class NotificatieprofielClient internal constructor(
    private val baseUrl: String,
    private val http: FbsHttpSupport
) {
    private val profielenUrl = "${baseUrl.trimEnd('/')}/api/v1/profielen"

    fun haalProfiel(
        ontvangerId: String,
        ontvangerIdType: OntvangerIdType,
        traceparent: String? = null
    ): Profiel {
        val uri = URI.create(
            "$profielenUrl/$ontvangerId?ontvangerIdType=${ontvangerIdType.name}"
        )
        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, Profiel::class.java)
    }

    fun werkProfielBij(
        ontvangerId: String,
        ontvangerIdType: OntvangerIdType,
        profiel: Profiel,
        traceparent: String? = null
    ): Profiel {
        val uri = URI.create(
            "$profielenUrl/$ontvangerId?ontvangerIdType=${ontvangerIdType.name}"
        )
        val request = http.requestBuilder(uri, traceparent)
            .header("Content-Type", "application/json")
            .PUT(http.jsonBody(profiel))
            .build()

        return http.execute(request, Profiel::class.java)
    }
}
