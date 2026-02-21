package nl.rijksoverheid.moz.berichtenmagazijn.exception

import com.fasterxml.jackson.core.JsonProcessingException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.common.model.ProblemDetail
import org.jboss.logging.Logger

@Provider
class ProblemDetailExceptionMapper : ExceptionMapper<Exception> {

    private val log = Logger.getLogger(ProblemDetailExceptionMapper::class.java)

    override fun toResponse(exception: Exception): Response {
        val (status, problem) = when (exception) {
            is BerichtNietGevondenException -> {
                log.debugf("Bericht niet gevonden: %s", exception.message)
                Response.Status.NOT_FOUND to ProblemDetail(
                    title = "Niet gevonden",
                    status = Response.Status.NOT_FOUND.statusCode,
                    detail = exception.message
                )
            }

            is IllegalArgumentException -> {
                log.debugf("Ongeldig verzoek: %s", exception.message)
                Response.Status.BAD_REQUEST to ProblemDetail(
                    title = "Ongeldig verzoek",
                    status = Response.Status.BAD_REQUEST.statusCode,
                    detail = exception.message
                )
            }

            is JsonProcessingException -> {
                log.debugf("Ongeldig JSON formaat: %s", exception.originalMessage)
                Response.Status.BAD_REQUEST to ProblemDetail(
                    title = "Ongeldig JSON formaat",
                    status = Response.Status.BAD_REQUEST.statusCode,
                    detail = "Het verzoek bevat ongeldige JSON"
                )
            }

            is WebApplicationException -> {
                val httpStatus = Response.Status.fromStatusCode(exception.response.status)
                if (httpStatus == null) {
                    log.warnf("Onbekende HTTP statuscode: %d", exception.response.status)
                }
                val effectiveStatus = httpStatus ?: Response.Status.INTERNAL_SERVER_ERROR
                if (effectiveStatus.family == Response.Status.Family.SERVER_ERROR) {
                    log.errorf(exception, "Server fout: status=%d", effectiveStatus.statusCode)
                }
                effectiveStatus to ProblemDetail(
                    title = effectiveStatus.reasonPhrase,
                    status = effectiveStatus.statusCode,
                    detail = if (effectiveStatus.family == Response.Status.Family.SERVER_ERROR)
                        "Er is een serverfout opgetreden" else exception.message
                )
            }

            else -> {
                log.errorf(exception, "Onverwachte fout [%s]: %s",
                    exception.javaClass.simpleName, exception.message)
                Response.Status.INTERNAL_SERVER_ERROR to ProblemDetail(
                    title = "Interne serverfout",
                    status = Response.Status.INTERNAL_SERVER_ERROR.statusCode,
                    detail = "Er is een onverwachte fout opgetreden"
                )
            }
        }

        return Response.status(status)
            .type(FbsConstants.MEDIA_TYPE_PROBLEM_JSON)
            .entity(problem)
            .build()
    }
}
