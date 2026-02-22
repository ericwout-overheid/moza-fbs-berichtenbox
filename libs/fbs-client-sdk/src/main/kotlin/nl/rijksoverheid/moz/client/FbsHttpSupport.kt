package nl.rijksoverheid.moz.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.common.model.ProblemDetail
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Gedeelde HTTP-infrastructuur voor alle FBS sub-clients.
 */
internal class FbsHttpSupport(
    val httpClient: HttpClient,
    val objectMapper: ObjectMapper,
    private val bearerToken: String?,
    private val requestTimeout: Duration
) {
    /**
     * Bouwt een basis HttpRequest.Builder met standaard headers.
     */
    fun requestBuilder(uri: URI, traceparent: String? = null): HttpRequest.Builder {
        val builder = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", "application/json")
            .timeout(requestTimeout)

        bearerToken?.let { builder.header("Authorization", "Bearer $it") }
        traceparent?.let { builder.header("traceparent", it) }

        return builder
    }

    /**
     * Maakt een JSON BodyPublisher van een object.
     */
    fun jsonBody(body: Any): HttpRequest.BodyPublisher {
        val json = objectMapper.writeValueAsString(body)
        return HttpRequest.BodyPublishers.ofString(json)
    }

    /**
     * Verstuurt een request en deserialiseert de response naar het opgegeven type.
     */
    fun <T> execute(
        request: HttpRequest,
        responseType: Class<T>,
        expectedStatus: Set<Int> = setOf(200)
    ): T {
        val response = send(request)
        checkStatus(response, expectedStatus)
        return deserialize(response, objectMapper.constructType(responseType))
    }

    /**
     * Verstuurt een request en deserialiseert de response naar een generiek type (bijv. Page<Bericht>).
     */
    fun <T> execute(
        request: HttpRequest,
        responseType: JavaType,
        expectedStatus: Set<Int> = setOf(200)
    ): T {
        val response = send(request)
        checkStatus(response, expectedStatus)
        return deserialize(response, responseType)
    }

    /**
     * Verstuurt een request zonder response body (bijv. DELETE 204).
     */
    fun executeNoContent(
        request: HttpRequest,
        expectedStatus: Set<Int> = setOf(204)
    ) {
        val response = send(request)
        checkStatus(response, expectedStatus)
    }

    /**
     * Construeert een JavaType voor generieke types zoals Page<T>.
     */
    fun constructPageType(elementType: Class<*>): JavaType {
        return objectMapper.typeFactory.constructParametricType(
            nl.rijksoverheid.moz.common.model.Page::class.java,
            elementType
        )
    }

    private fun send(request: HttpRequest): HttpResponse<String> {
        return try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw FbsException("Verzoek onderbroken: ${e.message}", cause = e)
        } catch (e: IOException) {
            throw FbsException("Fout bij communicatie met FBS service: ${e.message}", cause = e)
        }
    }

    private fun checkStatus(response: HttpResponse<String>, expectedStatus: Set<Int>) {
        if (response.statusCode() in expectedStatus) return

        val problemDetail = parseProblemDetail(response)
        val message = problemDetail?.detail
            ?: "FBS service retourneerde statuscode ${response.statusCode()}"

        throw FbsException(
            message = message,
            statusCode = response.statusCode(),
            problemDetail = problemDetail
        )
    }

    private fun parseProblemDetail(response: HttpResponse<String>): ProblemDetail? {
        val contentType = response.headers()
            .firstValue("Content-Type")
            .orElse("")

        if (!contentType.contains(FbsConstants.MEDIA_TYPE_PROBLEM_JSON)) return null

        return try {
            objectMapper.readValue(response.body(), ProblemDetail::class.java)
        } catch (_: JsonProcessingException) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deserialize(response: HttpResponse<String>, javaType: JavaType): T {
        return try {
            objectMapper.readValue(response.body(), javaType) as T
        } catch (e: JsonProcessingException) {
            throw FbsException(
                "Fout bij verwerken response: ${e.message}",
                statusCode = response.statusCode(),
                cause = e
            )
        }
    }

    companion object {
        fun create(
            bearerToken: String?,
            connectTimeout: Duration,
            requestTimeout: Duration,
            httpClient: HttpClient? = null
        ): FbsHttpSupport {
            val client = httpClient ?: HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build()

            val mapper = jacksonObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            return FbsHttpSupport(client, mapper, bearerToken, requestTimeout)
        }
    }
}
