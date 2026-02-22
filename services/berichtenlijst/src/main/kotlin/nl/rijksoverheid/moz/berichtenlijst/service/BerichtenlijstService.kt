package nl.rijksoverheid.moz.berichtenlijst.service

import io.quarkus.cache.CacheResult
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.berichtenlijst.client.BerichtenmagazijnClient
import nl.rijksoverheid.moz.berichtenlijst.mapping.BerichtRecordMapper
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.common.model.BerichtRecord
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Page
import nl.rijksoverheid.moz.common.util.PiiMasker
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.ldv.LdvVerwerking
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger
import java.net.URI

@ApplicationScoped
class BerichtenlijstService(
    @RestClient private val berichtenmagazijnClient: BerichtenmagazijnClient,
    private val ldvLogger: LdvLogger,
    @ConfigProperty(name = "quarkus.rest-client.berichtenmagazijn-api.url", defaultValue = "http://localhost:8080")
    private val magazijnBaseUrl: String
) {

    private val log = Logger.getLogger(BerichtenlijstService::class.java)

    fun haalBerichtenlijst(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        page: Int,
        pageSize: Int
    ): Page<BerichtRecord> {
        logLdvVerwerking(
            "https://fbs.nl/verwerkingen/berichtenlijst-ophalen",
            ontvangerId, ontvangerIdType, "haalBerichtenlijst"
        )
        return haalBerichtenlijstCached(ontvangerIdType, ontvangerId, page, pageSize)
    }

    @CacheResult(cacheName = "berichtenlijst-cache")
    internal fun haalBerichtenlijstCached(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        page: Int,
        pageSize: Int
    ): Page<BerichtRecord> {
        val effectivePage = maxOf(page, 1)
        val effectivePageSize = pageSize.coerceIn(1, FbsConstants.MAX_PAGE_SIZE)

        val berichtenPage = berichtenmagazijnClient.lijstBerichten(
            ontvangerIdType, ontvangerId, effectivePage, effectivePageSize, null
        )

        val records = berichtenPage.resultaten.map { bericht ->
            BerichtRecordMapper.toBerichtRecord(bericht, magazijnBaseUrl)
        }

        return Page(
            resultaten = records,
            pagina = berichtenPage.pagina,
            paginaGrootte = berichtenPage.paginaGrootte,
            totaalPaginas = berichtenPage.totaalPaginas,
            totaalElementen = berichtenPage.totaalElementen
        )
    }

    fun zoekBerichten(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        zoekterm: String,
        page: Int,
        pageSize: Int
    ): Page<BerichtRecord> {
        require(zoekterm.length >= 2) { "Zoekterm moet minimaal 2 tekens bevatten" }
        logLdvVerwerking(
            "https://fbs.nl/verwerkingen/berichtenlijst-zoeken",
            ontvangerId, ontvangerIdType, "zoekBerichten"
        )
        return zoekBerichtenCached(ontvangerIdType, ontvangerId, zoekterm, page, pageSize)
    }

    @CacheResult(cacheName = "berichtenlijst-zoek-cache")
    internal fun zoekBerichtenCached(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        zoekterm: String,
        page: Int,
        pageSize: Int
    ): Page<BerichtRecord> {
        val effectivePage = maxOf(page, 1)
        val effectivePageSize = pageSize.coerceIn(1, FbsConstants.MAX_PAGE_SIZE)

        val berichtenPage = berichtenmagazijnClient.lijstBerichten(
            ontvangerIdType, ontvangerId, effectivePage, effectivePageSize, onderwerp = zoekterm
        )

        val records = berichtenPage.resultaten
            .map { bericht -> BerichtRecordMapper.toBerichtRecord(bericht, magazijnBaseUrl) }

        return Page(
            resultaten = records,
            pagina = berichtenPage.pagina,
            paginaGrootte = berichtenPage.paginaGrootte,
            totaalPaginas = berichtenPage.totaalPaginas,
            totaalElementen = berichtenPage.totaalElementen
        )
    }

    private fun logLdvVerwerking(
        verwerkingsActiviteitUrl: String,
        ontvangerId: String,
        ontvangerIdType: OntvangerIdType,
        operatieNaam: String
    ) {
        try {
            ldvLogger.logVerwerking(
                LdvVerwerking(
                    verwerkingsActiviteitId = URI(verwerkingsActiviteitUrl),
                    betrokkeneId = ontvangerId,
                    betrokkeneIdType = ontvangerIdType.name,
                    operatieNaam = operatieNaam
                )
            )
        } catch (e: Exception) {
            // Breed catch-blok is intentioneel: LDV-logging is best-effort en mag de primaire functionaliteit niet blokkeren
            log.errorf(e, "LDV logging mislukt voor %s: ontvangerId=%s", operatieNaam, PiiMasker.mask(ontvangerId))
        }
    }
}
