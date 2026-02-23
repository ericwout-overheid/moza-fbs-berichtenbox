package nl.rijksoverheid.moz.admindashboard.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.client.FbsClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@ApplicationScoped
class FbsClientProducer(
    @param:ConfigProperty(name = "fbs.berichtenmagazijn.url")
    private val berichtenmagazijnUrl: String,

    @param:ConfigProperty(name = "fbs.berichtenlijst.url")
    private val berichtenlijstUrl: String,

    @param:ConfigProperty(name = "fbs.admin.bearer-token")
    private val bearerToken: String
) {
    private val log = Logger.getLogger(FbsClientProducer::class.java)

    @Produces
    @ApplicationScoped
    fun fbsClient(): FbsClient {
        if (bearerToken.isBlank()) {
            log.errorf("fbs.admin.bearer-token is leeg — stel FBS_ADMIN_BEARER_TOKEN omgevingsvariabele in. " +
                "Alle API-aanroepen zullen mislukken tot dit is geconfigureerd.")
        }
        log.infof(
            "FbsClient configuratie: magazijn=%s, lijst=%s",
            berichtenmagazijnUrl, berichtenlijstUrl
        )
        return try {
            FbsClient.builder()
                .berichtenmagazijnUrl(berichtenmagazijnUrl)
                .berichtenlijstUrl(berichtenlijstUrl)
                .bearerToken(bearerToken.ifBlank { null })
                .build()
        } catch (e: Exception) {
            log.errorf(e, "FbsClient initialisatie mislukt: %s", e.message)
            throw e
        }
    }
}
