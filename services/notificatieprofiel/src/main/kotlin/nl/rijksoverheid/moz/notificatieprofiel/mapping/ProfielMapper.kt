package nl.rijksoverheid.moz.notificatieprofiel.mapping

import nl.rijksoverheid.moz.common.model.Profiel
import nl.rijksoverheid.moz.notificatieprofiel.entity.ProfielEntity
import java.time.Instant

object ProfielMapper {

    fun toDto(entity: ProfielEntity): Profiel = Profiel(
        ontvangerId = entity.ontvangerId,
        ontvangerIdType = entity.ontvangerIdType,
        emailNotificaties = entity.emailNotificaties,
        smsNotificaties = entity.smsNotificaties,
        emailAdres = entity.emailAdres,
        telefoonnummer = entity.telefoonnummer,
        frequentie = entity.frequentie
    )

    fun updateEntity(entity: ProfielEntity, profiel: Profiel) {
        entity.emailNotificaties = profiel.emailNotificaties
        entity.smsNotificaties = profiel.smsNotificaties
        entity.emailAdres = profiel.emailAdres
        entity.telefoonnummer = profiel.telefoonnummer
        entity.frequentie = profiel.frequentie
        entity.bijgewerktOp = Instant.now()
    }
}
