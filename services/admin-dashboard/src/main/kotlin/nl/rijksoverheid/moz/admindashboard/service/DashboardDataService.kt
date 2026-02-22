package nl.rijksoverheid.moz.admindashboard.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.client.FbsClient
import nl.rijksoverheid.moz.client.FbsException
import nl.rijksoverheid.moz.common.model.*
import org.jboss.logging.Logger
import java.util.UUID

data class DashboardResult<T>(val data: T, val foutmelding: String? = null) {
    val isFout get() = foutmelding != null

    companion object {
        fun <T> ok(data: T) = DashboardResult(data)
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
            log.warnf(e, "Berichten ophalen mislukt: page=%d, pageSize=%d, statusCode=%s", page, pageSize, e.statusCode)
            DashboardResult.fout("Berichten konden niet worden opgehaald: ${e.message}", Page.leeg(pageSize))
        }

    fun haalBericht(id: UUID): DashboardResult<Bericht?> =
        try {
            DashboardResult.ok(fbsClient.berichten().haalBericht(id))
        } catch (e: FbsException) {
            log.warnf(e, "Bericht ophalen mislukt: berichtId=%s, statusCode=%s", id, e.statusCode)
            DashboardResult.fout("Bericht kon niet worden opgehaald: ${e.message}", null)
        }

    fun haalNotificatieStatus(id: UUID): DashboardResult<NotificatieStatus?> =
        try {
            DashboardResult.ok(fbsClient.notificaties().haalNotificatieStatus(id))
        } catch (e: FbsException) {
            log.warnf(e, "Notificatiestatus ophalen mislukt: notificatieId=%s, statusCode=%s", id, e.statusCode)
            DashboardResult.fout("Notificatiestatus kon niet worden opgehaald: ${e.message}", null)
        }

    fun haalBereikbaarheid(ontvangerId: String, type: OntvangerIdType): DashboardResult<Bereikbaarheid?> =
        try {
            DashboardResult.ok(fbsClient.bereikbaarheid().haalBereikbaarheid(ontvangerId, type))
        } catch (e: FbsException) {
            log.warnf(e, "Bereikbaarheid ophalen mislukt: ontvangerIdType=%s, statusCode=%s", type, e.statusCode)
            DashboardResult.fout("Bereikbaarheid kon niet worden opgehaald: ${e.message}", null)
        }

    fun haalProfiel(ontvangerId: String, type: OntvangerIdType): DashboardResult<Profiel?> =
        try {
            DashboardResult.ok(fbsClient.profielen().haalProfiel(ontvangerId, type))
        } catch (e: FbsException) {
            log.warnf(e, "Profiel ophalen mislukt: ontvangerIdType=%s, statusCode=%s", type, e.statusCode)
            DashboardResult.fout("Profiel kon niet worden opgehaald: ${e.message}", null)
        }
}
