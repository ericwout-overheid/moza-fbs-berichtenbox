package nl.rijksoverheid.moz.berichtenmagazijn.mapping

import nl.rijksoverheid.moz.berichtenmagazijn.entity.BerichtEntity
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BijlageEntity
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BijlageMetadata

object BerichtMapper {

    fun toDto(entity: BerichtEntity): Bericht = Bericht(
        id = entity.id,
        afzenderOin = entity.afzenderOin,
        ontvangerIdType = entity.ontvangerIdType,
        ontvangerId = entity.ontvangerId,
        onderwerp = entity.onderwerp,
        inhoud = entity.inhoud,
        status = entity.status,
        aangemaaktOp = entity.aangemaaktOp,
        gelezenOp = entity.gelezenOp,
        bijlagen = entity.bijlagen.map { toBijlageDto(it) }
    )

    fun toBijlageDto(entity: BijlageEntity): BijlageMetadata = BijlageMetadata(
        id = entity.id,
        bestandsnaam = entity.bestandsnaam,
        mediaType = entity.mediaType,
        grootte = entity.grootte,
        aangemaaktOp = entity.aangemaaktOp
    )
}
