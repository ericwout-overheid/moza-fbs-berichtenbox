package nl.rijksoverheid.moz.client

import nl.rijksoverheid.moz.common.model.ProblemDetail

/**
 * Exception voor fouten bij communicatie met FBS services.
 *
 * @property statusCode HTTP-statuscode (null bij verbindingsfouten)
 * @property problemDetail RFC 9457 problem detail (indien beschikbaar in response)
 */
class FbsException(
    message: String,
    val statusCode: Int? = null,
    val problemDetail: ProblemDetail? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    init {
        require(problemDetail == null || statusCode != null) {
            "problemDetail vereist een statusCode"
        }
    }
}
