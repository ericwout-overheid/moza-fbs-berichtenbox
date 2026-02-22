package nl.rijksoverheid.moz.client

import nl.rijksoverheid.moz.common.model.*
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpRequest
import java.util.UUID

/**
 * Client voor het Berichtenmagazijn API.
 *
 * @see <a href="openapi/berichtenmagazijn-v1.yaml">OpenAPI spec</a>
 */
class BerichtenClient internal constructor(
    private val baseUrl: String,
    private val http: FbsHttpSupport
) {
    private val berichtenUrl = "${baseUrl.trimEnd('/')}/api/v1/berichten"

    fun maakBericht(
        verzoek: BerichtAanmaakVerzoek,
        traceparent: String? = null
    ): Bericht {
        val request = http.requestBuilder(URI.create(berichtenUrl), traceparent)
            .header("Content-Type", "application/json")
            .POST(http.jsonBody(verzoek))
            .build()

        return http.execute(request, Bericht::class.java, setOf(201))
    }

    fun lijstBerichten(
        ontvangerIdType: OntvangerIdType? = null,
        ontvangerId: String? = null,
        status: BerichtStatus? = null,
        page: Int = 1,
        pageSize: Int = 20,
        traceparent: String? = null
    ): Page<Bericht> {
        val params = buildList {
            ontvangerIdType?.let { add("ontvangerIdType=${it.name}") }
            ontvangerId?.let { add("ontvangerId=${FbsHttpSupport.urlEncode(it)}") }
            status?.let { add("status=${it.name}") }
            add("page=$page")
            add("pageSize=$pageSize")
        }
        val uri = URI.create("$berichtenUrl?${params.joinToString("&")}")

        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, http.constructPageType(Bericht::class.java))
    }

    fun haalBericht(
        berichtId: UUID,
        traceparent: String? = null
    ): Bericht {
        val request = http.requestBuilder(URI.create("$berichtenUrl/$berichtId"), traceparent)
            .GET()
            .build()

        return http.execute(request, Bericht::class.java)
    }

    fun werkBerichtBij(
        berichtId: UUID,
        statusWijziging: BerichtStatusWijziging,
        traceparent: String? = null
    ): Bericht {
        val request = http.requestBuilder(URI.create("$berichtenUrl/$berichtId"), traceparent)
            .header("Content-Type", "application/json")
            .method("PATCH", http.jsonBody(statusWijziging))
            .build()

        return http.execute(request, Bericht::class.java)
    }

    fun verwijderBericht(
        berichtId: UUID,
        traceparent: String? = null
    ) {
        val request = http.requestBuilder(URI.create("$berichtenUrl/$berichtId"), traceparent)
            .DELETE()
            .build()

        http.executeNoContent(request)
    }

    fun lijstBijlagen(
        berichtId: UUID,
        traceparent: String? = null
    ): List<BijlageMetadata> {
        val uri = URI.create("$berichtenUrl/$berichtId/bijlagen")
        val request = http.requestBuilder(uri, traceparent)
            .GET()
            .build()

        return http.execute(request, http.constructListType(BijlageMetadata::class.java))
    }

    fun uploadBijlage(
        berichtId: UUID,
        bestandsnaam: String,
        mediaType: String,
        inhoud: InputStream,
        traceparent: String? = null
    ): BijlageMetadata {
        val boundary = "----FbsBoundary${System.nanoTime()}"
        val uri = URI.create("$berichtenUrl/$berichtId/bijlagen")

        val bytes = try {
            inhoud.readAllBytes()
        } catch (e: IOException) {
            throw FbsException(
                "Fout bij lezen van bijlage '$bestandsnaam': ${e.message}",
                cause = e
            )
        }
        val body = buildMultipartBody(boundary, bestandsnaam, mediaType, bytes)

        val request = http.requestBuilder(uri, traceparent)
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build()

        return http.execute(request, BijlageMetadata::class.java, setOf(201))
    }

    private fun buildMultipartBody(
        boundary: String,
        bestandsnaam: String,
        mediaType: String,
        bytes: ByteArray
    ): ByteArray {
        val sanitized = bestandsnaam
            .replace("\"", "_")
            .replace("\r", "")
            .replace("\n", "")
        val crlf = "\r\n"
        val builder = StringBuilder()
        builder.append("--$boundary$crlf")
        builder.append("Content-Disposition: form-data; name=\"bestand\"; filename=\"$sanitized\"$crlf")
        builder.append("Content-Type: $mediaType$crlf")
        builder.append(crlf)

        val header = builder.toString().toByteArray()
        val footer = "$crlf--$boundary--$crlf".toByteArray()

        return header + bytes + footer
    }
}
