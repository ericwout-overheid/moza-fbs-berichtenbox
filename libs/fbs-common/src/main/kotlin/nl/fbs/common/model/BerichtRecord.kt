package nl.fbs.common.model

import java.time.Instant
import java.util.UUID

/**
 * Lichtgewicht berichtrecord voor de berichtenlijst.
 *
 * Bevat alleen metadata, niet de volledige inhoud van het bericht.
 *
 * @property berichtId identifier van het bericht in het bronmagazijn
 * @property afzenderNaam naam van de afzender
 * @property afzenderOin OIN van de afzender
 * @property magazijnUrl URL van het berichtenmagazijn waar het bericht is opgeslagen
 * @property onderwerp onderwerp van het bericht
 * @property status huidige status van het bericht
 * @property aangemaaktOp tijdstip van aanmaak
 */
data class BerichtRecord(
    val berichtId: UUID,
    val afzenderNaam: String,
    val afzenderOin: String,
    val magazijnUrl: String,
    val onderwerp: String,
    val status: BerichtStatus,
    val aangemaaktOp: Instant
)
