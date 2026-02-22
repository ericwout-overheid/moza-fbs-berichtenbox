package nl.rijksoverheid.moz.berichtenlijst.exception

import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import nl.rijksoverheid.moz.common.exception.BaseProblemDetailExceptionMapper
import nl.rijksoverheid.moz.common.model.ProblemDetail
import org.jboss.logging.Logger

@Provider
class ProblemDetailExceptionMapper : BaseProblemDetailExceptionMapper() {

    private val log = Logger.getLogger(ProblemDetailExceptionMapper::class.java)

    override fun mapServiceException(exception: Exception): Pair<Response.Status, ProblemDetail>? {
        return when (exception) {
            is ProcessingException -> {
                log.errorf(exception, "Berichtenmagazijn niet bereikbaar")
                Response.Status.BAD_GATEWAY to ProblemDetail(
                    title = "Bad Gateway",
                    status = Response.Status.BAD_GATEWAY.statusCode,
                    detail = "Het berichtenmagazijn is niet bereikbaar"
                )
            }

            else -> null
        }
    }
}
