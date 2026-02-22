package nl.rijksoverheid.moz.client

import nl.rijksoverheid.moz.common.model.BerichtRecord
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Page
import java.net.URI

/**
 * Client voor het Berichtenlijst API.
 *
 * @see <a href="openapi/berichtenlijst-v1.yaml">OpenAPI spec</a>
 */
class BerichtenlijstClient internal constructor(
    private val baseUrl: String,
    private val http: FbsHttpSupport
) {
    private val berichtenlijstUrl = "${baseUrl.trimEnd('/')}/api/v1/berichtenlijst"

    fun haalBerichtenlijst(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        page: Int = 1,
        pageSize: Int = 20,
        traceparent: String? = null
    ): Page<BerichtRecord> {
        val uri = URI.create(
            "$berichtenlijstUrl?ontvangerIdType=${ontvangerIdType.name}" +
                "&ontvangerId=$ontvangerId&page=$page&pageSize=$pageSize"
        )
        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, http.constructPageType(BerichtRecord::class.java))
    }

    fun zoekBerichten(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        zoekterm: String,
        page: Int = 1,
        pageSize: Int = 20,
        traceparent: String? = null
    ): Page<BerichtRecord> {
        val uri = URI.create(
            "$berichtenlijstUrl/zoek?ontvangerIdType=${ontvangerIdType.name}" +
                "&ontvangerId=$ontvangerId&zoekterm=$zoekterm&page=$page&pageSize=$pageSize"
        )
        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, http.constructPageType(BerichtRecord::class.java))
    }
}
