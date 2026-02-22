package nl.rijksoverheid.moz.admindashboard.service

import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.client.FbsClient
import nl.rijksoverheid.moz.client.FbsException
import nl.rijksoverheid.moz.common.model.*
import java.util.UUID

@ApplicationScoped
class DashboardDataService(
    private val fbsClient: FbsClient
) {
    fun haalBerichten(page: Int = 1, pageSize: Int = 20): Page<Bericht> =
        try {
            fbsClient.berichten().lijstBerichten(page = page, pageSize = pageSize)
        } catch (e: FbsException) {
            Page.leeg(pageSize)
        }

    fun haalBericht(id: UUID): Bericht? =
        try {
            fbsClient.berichten().haalBericht(id)
        } catch (e: FbsException) {
            null
        }

    fun haalNotificatieStatus(id: UUID): NotificatieStatus? =
        try {
            fbsClient.notificaties().haalNotificatieStatus(id)
        } catch (e: FbsException) {
            null
        }

    fun haalBereikbaarheid(ontvangerId: String, type: OntvangerIdType): Bereikbaarheid? =
        try {
            fbsClient.bereikbaarheid().haalBereikbaarheid(ontvangerId, type)
        } catch (e: FbsException) {
            null
        }

    fun haalProfiel(ontvangerId: String, type: OntvangerIdType): Profiel? =
        try {
            fbsClient.profielen().haalProfiel(ontvangerId, type)
        } catch (e: FbsException) {
            null
        }
}
