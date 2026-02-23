package nl.rijksoverheid.moz.admindashboard.service

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class ServiceStatus(
    val naam: String,
    val url: String,
    val beschikbaar: Boolean,
    val statusCode: Int? = null,
    val foutmelding: String? = null,
    val responseTimeMs: Long? = null
) {
    init {
        if (beschikbaar) {
            require(statusCode != null) { "beschikbare service moet statusCode hebben" }
            require(foutmelding == null) { "beschikbare service mag geen foutmelding hebben" }
        }
    }
}

@ApplicationScoped
class ServiceHealthChecker(
    @param:ConfigProperty(name = "fbs.health.urls")
    private val healthUrls: List<String>
) {
    private val log = Logger.getLogger(ServiceHealthChecker::class.java)

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build()

    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    fun checkAll(): List<ServiceStatus> {
        val futures = healthUrls.map { url ->
            executor.submit<ServiceStatus> { checkService(url) }
        }
        return futures.map { it.get(10, TimeUnit.SECONDS) }
    }

    private fun checkService(baseUrl: String): ServiceStatus {
        val naam = mapPortToNaam(baseUrl)
        val healthUrl = "${baseUrl.trimEnd('/')}/q/health"

        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(healthUrl))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build()

            val start = System.currentTimeMillis()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val elapsed = System.currentTimeMillis() - start

            ServiceStatus(
                naam = naam,
                url = baseUrl,
                beschikbaar = response.statusCode() in 200..299,
                statusCode = response.statusCode(),
                responseTimeMs = elapsed
            )
        } catch (e: IOException) {
            log.warnf("Health check mislukt voor %s (%s): %s", naam, healthUrl, e.message)
            ServiceStatus(
                naam = naam,
                url = baseUrl,
                beschikbaar = false,
                foutmelding = e.message ?: "Verbindingsfout"
            )
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            log.warnf("Health check onderbroken voor %s (%s)", naam, healthUrl)
            ServiceStatus(
                naam = naam,
                url = baseUrl,
                beschikbaar = false,
                foutmelding = "Verzoek onderbroken"
            )
        } catch (e: Exception) {
            log.errorf(e, "Onverwachte fout bij health check voor %s (%s): %s", naam, healthUrl, e.message)
            ServiceStatus(
                naam = naam,
                url = baseUrl,
                beschikbaar = false,
                foutmelding = "Onverwachte fout: ${e.message}"
            )
        }
    }

    internal companion object {
        private val PORT_NAAM_MAP = mapOf(
            8080 to "Berichtenmagazijn",
            8081 to "Berichtenlijst"
        )

        fun mapPortToNaam(url: String): String {
            val uri = try {
                URI.create(url)
            } catch (e: IllegalArgumentException) {
                return "Onbekend"
            }
            return PORT_NAAM_MAP[uri.port] ?: uri.host ?: "Onbekend (${uri.port})"
        }
    }
}
