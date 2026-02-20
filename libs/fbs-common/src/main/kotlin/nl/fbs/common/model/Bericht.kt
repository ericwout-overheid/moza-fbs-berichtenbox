package nl.fbs.common.model

import java.time.Instant
import java.util.UUID

/**
 * Volledig bericht zoals opgeslagen in het berichtenmagazijn.
 *
 * @property id unieke identifier van het bericht
 * @property afzenderOin OIN van de afzender
 * @property ontvangerIdType type identifier van de ontvanger
 * @property ontvangerId identifier van de ontvanger
 * @property onderwerp onderwerp van het bericht
 * @property inhoud inhoud van het bericht
 * @property status huidige status van het bericht
 * @property aangemaaktOp tijdstip van aanmaak
 * @property gelezenOp tijdstip waarop het bericht is gelezen (null indien ongelezen)
 * @property bijlagen metadata van eventuele bijlagen
 */
data class Bericht(
    val id: UUID,
    val afzenderOin: String,
    val ontvangerIdType: OntvangerIdType,
    val ontvangerId: String,
    val onderwerp: String,
    val inhoud: String,
    val status: BerichtStatus,
    val aangemaaktOp: Instant,
    val gelezenOp: Instant? = null,
    val bijlagen: List<BijlageMetadata> = emptyList()
)
