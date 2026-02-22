package nl.rijksoverheid.moz.digitalebereikbaarheid.exception

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
            is BereikbaarheidNietGevondenException -> {
                log.debugf("Bereikbaarheid niet gevonden: %s", exception.message)
                Response.Status.NOT_FOUND to ProblemDetail(
                    title = "Niet gevonden",
                    status = Response.Status.NOT_FOUND.statusCode,
                    detail = "Geen bereikbaarheidsstatus gevonden voor de opgegeven ontvanger"
                )
            }

            else -> null
        }
    }
}
