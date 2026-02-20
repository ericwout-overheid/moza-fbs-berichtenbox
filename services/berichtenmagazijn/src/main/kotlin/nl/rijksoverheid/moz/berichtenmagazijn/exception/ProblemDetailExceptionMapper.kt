package nl.rijksoverheid.moz.berichtenmagazijn.exception

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
            is BerichtNietGevondenException -> Response.Status.NOT_FOUND to ProblemDetail(
                title = "Niet gevonden",
                status = Response.Status.NOT_FOUND.statusCode,
                detail = exception.message
            )

            is IllegalArgumentException -> Response.Status.BAD_REQUEST to ProblemDetail(
                title = "Ongeldig verzoek",
                status = Response.Status.BAD_REQUEST.statusCode,
                detail = exception.message
            )

            is WebApplicationException -> {
                val httpStatus = Response.Status.fromStatusCode(exception.response.status)
                    ?: Response.Status.INTERNAL_SERVER_ERROR
                httpStatus to ProblemDetail(
                    title = httpStatus.reasonPhrase,
                    status = httpStatus.statusCode,
                    detail = exception.message
                )
            }

            else -> {
                log.error("Onverwachte fout", exception)
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
