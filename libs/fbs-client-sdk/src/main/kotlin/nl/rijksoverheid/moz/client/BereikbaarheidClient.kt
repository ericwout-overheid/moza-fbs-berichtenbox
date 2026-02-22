package nl.rijksoverheid.moz.client

import nl.rijksoverheid.moz.common.model.Bereikbaarheid
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import java.net.URI

/**
 * Client voor het Digitale Bereikbaarheid API.
 *
 * @see <a href="openapi/digitale-bereikbaarheid-v1.yaml">OpenAPI spec</a>
 */
class BereikbaarheidClient internal constructor(
    private val baseUrl: String,
    private val http: FbsHttpSupport
) {
    private val bereikbaarheidUrl = "${baseUrl.trimEnd('/')}/api/v1/bereikbaarheid"

    fun haalBereikbaarheid(
        ontvangerId: String,
        ontvangerIdType: OntvangerIdType,
        traceparent: String? = null
    ): Bereikbaarheid {
        val uri = URI.create(
            "$bereikbaarheidUrl/${FbsHttpSupport.urlEncode(ontvangerId)}?ontvangerIdType=${ontvangerIdType.name}"
        )
        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, Bereikbaarheid::class.java)
    }

    fun registreerBereikbaarheid(
        ontvangerId: String,
        ontvangerIdType: OntvangerIdType,
        bereikbaarheid: Bereikbaarheid,
        traceparent: String? = null
    ): Bereikbaarheid {
        val uri = URI.create(
            "$bereikbaarheidUrl/${FbsHttpSupport.urlEncode(ontvangerId)}?ontvangerIdType=${ontvangerIdType.name}"
        )
        val request = http.requestBuilder(uri, traceparent)
            .header("Content-Type", "application/json")
            .PUT(http.jsonBody(bereikbaarheid))
            .build()

        return http.execute(request, Bereikbaarheid::class.java)
    }
}
