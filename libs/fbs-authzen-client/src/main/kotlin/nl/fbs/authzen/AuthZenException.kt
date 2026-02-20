package nl.fbs.authzen

/**
 * Exception voor fouten bij communicatie met de AuthZEN PDP.
 *
 * @property statusCode HTTP-statuscode (null bij verbindingsfouten)
 */
class AuthZenException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
