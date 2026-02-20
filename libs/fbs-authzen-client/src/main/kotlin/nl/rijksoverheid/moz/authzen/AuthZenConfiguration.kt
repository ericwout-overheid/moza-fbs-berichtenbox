package nl.rijksoverheid.moz.authzen

import java.time.Duration

/**
 * Configuratie voor de AuthZEN client.
 *
 * @property pdpUrl basis-URL van de Policy Decision Point
 * @property connectTimeout verbindingstimeout
 * @property requestTimeout request timeout
 */
data class AuthZenConfiguration(
    val pdpUrl: String,
    val connectTimeout: Duration = Duration.ofSeconds(5),
    val requestTimeout: Duration = Duration.ofSeconds(10)
) {
    /**
     * Volledig endpoint URL voor het evaluation API.
     */
    val evaluationEndpoint: String
        get() = "${pdpUrl.trimEnd('/')}/access/v1/evaluation"
}
