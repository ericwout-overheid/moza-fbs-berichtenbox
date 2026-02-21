package nl.rijksoverheid.moz.notificatieprofiel.mapping

import nl.rijksoverheid.moz.common.model.Profiel
import nl.rijksoverheid.moz.notificatieprofiel.entity.ProfielEntity

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
        entity.werkBij(profiel)
    }
}
