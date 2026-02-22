package nl.rijksoverheid.moz.client

import java.net.URI
import java.net.http.HttpClient
import java.time.Duration

/**
 * Facade voor alle FBS services.
 *
 * Gebruik [builder] om een instantie te maken:
 * ```kotlin
 * val client = FbsClient.builder()
 *     .berichtenmagazijnUrl("http://localhost:8080")
 *     .bearerToken("eyJ...")
 *     .build()
 *
 * val bericht = client.berichten().haalBericht(berichtId)
 * ```
 */
class FbsClient private constructor(
    private val berichtenClient: BerichtenClient,
    private val berichtenlijstClient: BerichtenlijstClient,
    private val notificatieClient: NotificatieClient,
    private val notificatieprofielClient: NotificatieprofielClient,
    private val bereikbaarheidClient: BereikbaarheidClient
) {
    fun berichten(): BerichtenClient = berichtenClient
    fun berichtenlijst(): BerichtenlijstClient = berichtenlijstClient
    fun notificaties(): NotificatieClient = notificatieClient
    fun profielen(): NotificatieprofielClient = notificatieprofielClient
    fun bereikbaarheid(): BereikbaarheidClient = bereikbaarheidClient

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var berichtenmagazijnUrl: String? = null
        private var berichtenlijstUrl: String? = null
        private var notificatieUrl: String? = null
        private var notificatieprofielUrl: String? = null
        private var bereikbaarheidUrl: String? = null
        private var bearerToken: String? = null
        private var connectTimeout: Duration = Duration.ofSeconds(5)
        private var requestTimeout: Duration = Duration.ofSeconds(30)
        private var httpClient: HttpClient? = null

        fun berichtenmagazijnUrl(url: String) = apply { this.berichtenmagazijnUrl = url }
        fun berichtenlijstUrl(url: String) = apply { this.berichtenlijstUrl = url }
        fun notificatieUrl(url: String) = apply { this.notificatieUrl = url }
        fun notificatieprofielUrl(url: String) = apply { this.notificatieprofielUrl = url }
        fun bereikbaarheidUrl(url: String) = apply { this.bereikbaarheidUrl = url }
        fun bearerToken(token: String?) = apply { this.bearerToken = token }
        fun connectTimeout(timeout: Duration) = apply { this.connectTimeout = timeout }
        fun requestTimeout(timeout: Duration) = apply { this.requestTimeout = timeout }
        fun httpClient(client: HttpClient) = apply { this.httpClient = client }

        fun build(): FbsClient {
            val magazijnUrl = requireNotNull(berichtenmagazijnUrl) {
                "berichtenmagazijnUrl is verplicht"
            }

            val resolvedBerichtenlijstUrl = berichtenlijstUrl ?: deriveUrl(magazijnUrl, 8081)
            val resolvedNotificatieUrl = notificatieUrl ?: deriveUrl(magazijnUrl, 8082)
            val resolvedNotificatieprofielUrl = notificatieprofielUrl ?: deriveUrl(magazijnUrl, 8083)
            val resolvedBereikbaarheidUrl = bereikbaarheidUrl ?: deriveUrl(magazijnUrl, 8084)

            val http = FbsHttpSupport.create(bearerToken, connectTimeout, requestTimeout, httpClient)

            return FbsClient(
                berichtenClient = BerichtenClient(magazijnUrl, http),
                berichtenlijstClient = BerichtenlijstClient(resolvedBerichtenlijstUrl, http),
                notificatieClient = NotificatieClient(resolvedNotificatieUrl, http),
                notificatieprofielClient = NotificatieprofielClient(resolvedNotificatieprofielUrl, http),
                bereikbaarheidClient = BereikbaarheidClient(resolvedBereikbaarheidUrl, http)
            )
        }

        internal companion object {
            fun deriveUrl(baseUrl: String, targetPort: Int): String {
                val uri = URI.create(baseUrl.trimEnd('/'))
                val port = if (uri.port == -1) {
                    if (uri.scheme == "https") 443 else 80
                } else {
                    uri.port
                }
                return if (port == targetPort) {
                    baseUrl
                } else {
                    "${uri.scheme}://${uri.host}:$targetPort"
                }
            }
        }
    }
}
