package nl.rijksoverheid.moz.client

import java.time.Duration

/**
 * Configuratie voor de FBS Client SDK.
 *
 * @property berichtenmagazijnUrl basis-URL van het berichtenmagazijn (port 8080)
 * @property berichtenlijstUrl basis-URL van de berichtenlijst service (port 8081)
 * @property notificatieUrl basis-URL van de notificatie service (port 8082)
 * @property notificatieprofielUrl basis-URL van de notificatieprofiel service (port 8083)
 * @property bereikbaarheidUrl basis-URL van de digitale bereikbaarheid service (port 8084)
 * @property bearerToken optioneel Bearer token voor authenticatie
 * @property connectTimeout verbindingstimeout
 * @property requestTimeout request timeout
 */
data class FbsConfiguration(
    val berichtenmagazijnUrl: String,
    val berichtenlijstUrl: String = "",
    val notificatieUrl: String = "",
    val notificatieprofielUrl: String = "",
    val bereikbaarheidUrl: String = "",
    val bearerToken: String? = null,
    val connectTimeout: Duration = Duration.ofSeconds(5),
    val requestTimeout: Duration = Duration.ofSeconds(30)
) {
    init {
        require(berichtenmagazijnUrl.isNotBlank()) { "berichtenmagazijnUrl mag niet leeg zijn" }
    }
}
