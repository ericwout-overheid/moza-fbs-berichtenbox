package nl.fbs.common.model

import java.time.Instant
import java.util.UUID

/**
 * Lichtgewicht berichtrecord voor de berichtenlijst.
 *
 * Bevat alleen metadata, niet de volledige inhoud van het bericht.
 *
 * @property berichtId identifier van het bericht in het bronmagazijn
 * @property afzenderOin OIN van de afzender
 * @property afzenderNaam naam van de afzender
 * @property onderwerp onderwerp van het bericht
 * @property status huidige status van het bericht
 * @property aangemaaktOp tijdstip van aanmaak
 * @property gelezenOp tijdstip waarop het bericht is gelezen (null indien ongelezen)
 * @property magazijnUrl URL van het berichtenmagazijn waar het bericht is opgeslagen
 */
data class BerichtRecord(
    val berichtId: UUID,
    val afzenderOin: String,
    val afzenderNaam: String,
    val onderwerp: String,
    val status: BerichtStatus,
    val aangemaaktOp: Instant,
    val gelezenOp: Instant? = null,
    val magazijnUrl: String
)
