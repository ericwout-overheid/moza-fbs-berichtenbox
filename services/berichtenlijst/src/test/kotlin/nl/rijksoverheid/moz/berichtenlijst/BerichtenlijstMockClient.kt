package nl.rijksoverheid.moz.berichtenlijst

import io.quarkus.test.Mock
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.berichtenlijst.client.BerichtenmagazijnClient
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Page
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.Instant
import java.util.UUID

@Mock
@ApplicationScoped
@RestClient
class BerichtenlijstMockClient : BerichtenmagazijnClient {

    override fun lijstBerichten(
        ontvangerIdType: OntvangerIdType,
        ontvangerId: String,
        page: Int,
        pageSize: Int
    ): Page<Bericht> {
        val berichten = listOf(
            Bericht(
                id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                afzenderOin = "00000001234567890000",
                ontvangerIdType = ontvangerIdType,
                ontvangerId = ontvangerId,
                onderwerp = "Belastingaanslag 2025",
                inhoud = "Uw belastingaanslag voor het jaar 2025.",
                status = BerichtStatus.NIEUW,
                aangemaaktOp = Instant.parse("2025-01-15T10:00:00Z")
            ),
            Bericht(
                id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                afzenderOin = "00000009876543210000",
                ontvangerIdType = ontvangerIdType,
                ontvangerId = ontvangerId,
                onderwerp = "Vergunning verleend",
                inhoud = "Uw vergunningaanvraag is goedgekeurd.",
                status = BerichtStatus.GELEZEN,
                aangemaaktOp = Instant.parse("2025-02-01T14:30:00Z"),
                gelezenOp = Instant.parse("2025-02-02T09:00:00Z")
            )
        )

        return Page(
            resultaten = berichten,
            pagina = page,
            paginaGrootte = pageSize,
            totaalPaginas = 1,
            totaalElementen = berichten.size.toLong()
        )
    }
}
