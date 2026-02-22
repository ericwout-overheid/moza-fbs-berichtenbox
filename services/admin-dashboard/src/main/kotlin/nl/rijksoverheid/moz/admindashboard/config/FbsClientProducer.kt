package nl.rijksoverheid.moz.admindashboard.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.client.FbsClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@ApplicationScoped
class FbsClientProducer {

    private val log = Logger.getLogger(FbsClientProducer::class.java)

    @ConfigProperty(name = "fbs.berichtenmagazijn.url")
    lateinit var berichtenmagazijnUrl: String

    @ConfigProperty(name = "fbs.berichtenlijst.url")
    lateinit var berichtenlijstUrl: String

    @ConfigProperty(name = "fbs.notificatie.url")
    lateinit var notificatieUrl: String

    @ConfigProperty(name = "fbs.notificatieprofiel.url")
    lateinit var notificatieprofielUrl: String

    @ConfigProperty(name = "fbs.bereikbaarheid.url")
    lateinit var bereikbaarheidUrl: String

    @ConfigProperty(name = "fbs.admin.bearer-token")
    lateinit var bearerToken: String

    @Produces
    @ApplicationScoped
    fun fbsClient(): FbsClient {
        log.infof(
            "FbsClient configuratie: magazijn=%s, lijst=%s, notificatie=%s, profiel=%s, bereikbaarheid=%s",
            berichtenmagazijnUrl, berichtenlijstUrl, notificatieUrl, notificatieprofielUrl, bereikbaarheidUrl
        )
        return FbsClient.builder()
            .berichtenmagazijnUrl(berichtenmagazijnUrl)
            .berichtenlijstUrl(berichtenlijstUrl)
            .notificatieUrl(notificatieUrl)
            .notificatieprofielUrl(notificatieprofielUrl)
            .bereikbaarheidUrl(bereikbaarheidUrl)
            .bearerToken(bearerToken)
            .build()
    }
}
