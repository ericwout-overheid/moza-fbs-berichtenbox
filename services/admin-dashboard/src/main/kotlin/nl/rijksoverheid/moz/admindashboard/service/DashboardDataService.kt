package nl.rijksoverheid.moz.admindashboard.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.client.FbsClient
import nl.rijksoverheid.moz.client.FbsException
import nl.rijksoverheid.moz.common.model.*
import org.jboss.logging.Logger
import java.util.UUID

class DashboardResult<T> private constructor(val data: T, val foutmelding: String?) {
    val isFout get() = foutmelding != null

    companion object {
        fun <T> ok(data: T) = DashboardResult(data, null)
        fun <T> fout(melding: String, fallback: T) = DashboardResult(fallback, melding)
    }
}

@ApplicationScoped
class DashboardDataService(
    private val fbsClient: FbsClient
) {
    private val log = Logger.getLogger(DashboardDataService::class.java)

    fun haalBerichten(
        page: Int = 1,
        pageSize: Int = 20,
        status: BerichtStatus? = null
    ): DashboardResult<Page<Bericht>> =
        try {
            DashboardResult.ok(
                fbsClient.berichten().lijstBerichten(page = page, pageSize = pageSize, status = status)
            )
        } catch (e: FbsException) {
            logFbsException(e, "Berichten ophalen mislukt: page=%d, pageSize=%d, statusCode=%s", page, pageSize, e.statusCode)
            DashboardResult.fout("Berichten konden niet worden opgehaald: ${e.message}", Page.leeg(pageSize))
        }

    fun haalBericht(id: UUID): DashboardResult<Bericht?> =
        try {
            DashboardResult.ok(fbsClient.berichten().haalBericht(id))
        } catch (e: FbsException) {
            logFbsException(e, "Bericht ophalen mislukt: berichtId=%s, statusCode=%s", id, e.statusCode)
            DashboardResult.fout("Bericht kon niet worden opgehaald: ${e.message}", null)
        }

    private fun logFbsException(e: FbsException, format: String, vararg params: Any?) {
        val statusCode = e.statusCode
        if (statusCode == null || statusCode >= 500) {
            log.errorf(e, format, *params)
        } else {
            log.warnf(e, format, *params)
        }
    }
}
