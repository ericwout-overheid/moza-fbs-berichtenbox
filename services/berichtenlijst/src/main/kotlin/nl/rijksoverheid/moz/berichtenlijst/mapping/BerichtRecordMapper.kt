package nl.rijksoverheid.moz.berichtenlijst.mapping

import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtRecord

object BerichtRecordMapper {

    fun toBerichtRecord(bericht: Bericht, magazijnBaseUrl: String): BerichtRecord = BerichtRecord(
        berichtId = bericht.id,
        afzenderOin = bericht.afzenderOin,
        afzenderNaam = bericht.afzenderOin, // TODO: resolve OIN naar organisatienaam via organisatieregister
        onderwerp = bericht.onderwerp,
        status = bericht.status,
        aangemaaktOp = bericht.aangemaaktOp,
        gelezenOp = bericht.gelezenOp,
        magazijnUrl = "$magazijnBaseUrl/api/v1/berichten/${bericht.id}"
    )
}
