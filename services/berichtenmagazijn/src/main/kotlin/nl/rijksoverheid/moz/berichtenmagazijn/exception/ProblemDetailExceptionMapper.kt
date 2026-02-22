package nl.rijksoverheid.moz.berichtenmagazijn.exception

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
            is BerichtNietGevondenException -> {
                log.debugf("Bericht niet gevonden: %s", exception.message)
                Response.Status.NOT_FOUND to ProblemDetail(
                    title = "Niet gevonden",
                    status = Response.Status.NOT_FOUND.statusCode,
                    detail = "Bericht niet gevonden"
                )
            }

            is StorageException -> {
                log.errorf(exception, "Storage fout: %s", exception.message)
                Response.Status.BAD_GATEWAY to ProblemDetail(
                    title = "Bad Gateway",
                    status = Response.Status.BAD_GATEWAY.statusCode,
                    detail = "De objectopslag is niet bereikbaar"
                )
            }

            else -> null
        }
    }
}
