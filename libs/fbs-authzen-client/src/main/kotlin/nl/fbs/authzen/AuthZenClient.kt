package nl.fbs.authzen

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import nl.fbs.authzen.model.EvaluationRequest
import nl.fbs.authzen.model.EvaluationResponse
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * AuthZEN client voor Federatieve Toegangsverlening (FTV).
 *
 * Communiceert met een Policy Decision Point (PDP) via het AuthZEN evaluation API
 * conform de NLGov AuthZEN specificatie.
 *
 * @see <a href="https://logius-standaarden.github.io/authzen-nlgov/">AuthZEN NLGov</a>
 */
class AuthZenClient(
    private val configuration: AuthZenConfiguration,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(configuration.connectTimeout)
        .build()
) {
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Evalueert een autorisatieverzoek bij de PDP.
     *
     * @param request het evaluatieverzoek
     * @param traceparent optionele W3C traceparent header voor tracing
     * @return de evaluatierespons met de autorisatiebeslissing
     * @throws AuthZenException bij verbindingsfouten (statusCode is null), onverwachte HTTP-statuscodes, of ongeldige JSON in de response
     */
    fun evaluate(request: EvaluationRequest, traceparent: String? = null): EvaluationResponse {
        val requestBody = objectMapper.writeValueAsString(request)

        val httpRequestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(configuration.evaluationEndpoint))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .timeout(configuration.requestTimeout)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))

        traceparent?.let { httpRequestBuilder.header("traceparent", it) }

        val httpRequest = httpRequestBuilder.build()

        val response = try {
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw AuthZenException("PDP-verzoek onderbroken: ${e.message}", cause = e)
        } catch (e: IOException) {
            throw AuthZenException(
                "Fout bij communicatie met PDP: ${e.message}",
                cause = e
            )
        }

        if (response.statusCode() != 200) {
            throw AuthZenException(
                "PDP retourneerde statuscode ${response.statusCode()}",
                statusCode = response.statusCode()
            )
        }

        return try {
            objectMapper.readValue<EvaluationResponse>(response.body())
        } catch (e: JsonProcessingException) {
            throw AuthZenException(
                "Fout bij verwerken PDP response: ${e.message}",
                statusCode = response.statusCode(),
                cause = e
            )
        }
    }
}
