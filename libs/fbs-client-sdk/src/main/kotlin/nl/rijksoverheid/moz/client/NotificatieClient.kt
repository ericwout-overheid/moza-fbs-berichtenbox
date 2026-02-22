package nl.rijksoverheid.moz.client

import nl.rijksoverheid.moz.common.model.Notificatie
import nl.rijksoverheid.moz.common.model.NotificatieStatus
import nl.rijksoverheid.moz.common.model.NotificatieVerzoek
import java.net.URI
import java.util.UUID

/**
 * Client voor het Notificatie API.
 *
 * @see <a href="openapi/notificatie-v1.yaml">OpenAPI spec</a>
 */
class NotificatieClient internal constructor(
    private val baseUrl: String,
    private val http: FbsHttpSupport
) {
    private val notificatiesUrl = "${baseUrl.trimEnd('/')}/api/v1/notificaties"

    fun verstuurNotificatie(
        verzoek: NotificatieVerzoek,
        traceparent: String? = null
    ): Notificatie {
        val request = http.requestBuilder(URI.create(notificatiesUrl), traceparent)
            .header("Content-Type", "application/json")
            .POST(http.jsonBody(verzoek))
            .build()

        return http.execute(request, Notificatie::class.java, setOf(202))
    }

    fun haalNotificatieStatus(
        notificatieId: UUID,
        traceparent: String? = null
    ): NotificatieStatus {
        val uri = URI.create("$notificatiesUrl/$notificatieId/status")
        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, NotificatieStatus::class.java)
    }
}
