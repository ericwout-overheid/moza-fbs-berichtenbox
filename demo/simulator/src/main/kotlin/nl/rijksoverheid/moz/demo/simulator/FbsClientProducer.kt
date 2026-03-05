package nl.rijksoverheid.moz.demo.simulator

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import nl.rijksoverheid.moz.client.FbsClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Optional

@ApplicationScoped
class FbsClientProducer(
    @param:ConfigProperty(name = "fbs.berichtenmagazijn.url")
    private val berichtenmagazijnUrl: String,

    @param:ConfigProperty(name = "fbs.berichtenlijst.url")
    private val berichtenlijstUrl: String,

    @param:ConfigProperty(name = "fbs.admin.bearer-token")
    private val bearerToken: Optional<String>
) {
    private val log = Logger.getLogger(FbsClientProducer::class.java)

    @Produces
    @ApplicationScoped
    fun fbsClient(): FbsClient {
        log.infof("FbsClient configuratie: magazijn=%s, lijst=%s",
            berichtenmagazijnUrl, berichtenlijstUrl)
        val token = bearerToken.orElse(null)?.ifBlank { null }
        return FbsClient.builder()
            .berichtenmagazijnUrl(berichtenmagazijnUrl)
            .berichtenlijstUrl(berichtenlijstUrl)
            .bearerToken(token)
            .build()
    }
}
