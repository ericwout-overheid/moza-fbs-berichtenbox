package nl.rijksoverheid.moz.notificatie.mapping

import nl.rijksoverheid.moz.common.model.Notificatie
import nl.rijksoverheid.moz.common.model.NotificatieStatus
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity

object NotificatieMapper {

    fun toDto(entity: NotificatieEntity): Notificatie = Notificatie(
        id = entity.id,
        ontvangerIdType = entity.ontvangerIdType,
        ontvangerId = entity.ontvangerId,
        kanaal = entity.kanaal,
        onderwerp = entity.onderwerp,
        inhoud = entity.inhoud,
        status = entity.status,
        aangemaaktOp = entity.aangemaaktOp,
        verzondenOp = entity.verzondenOp,
        afgeleverdOp = entity.afgeleverdOp
    )

    fun toStatusDto(entity: NotificatieEntity): NotificatieStatus = NotificatieStatus(
        notificatieId = entity.id,
        status = entity.status,
        verzondenOp = entity.verzondenOp,
        afgeleverdOp = entity.afgeleverdOp,
        foutmelding = entity.foutmelding
    )
}
