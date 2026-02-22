package nl.rijksoverheid.moz.digitalebereikbaarheid.mapping

import nl.rijksoverheid.moz.common.model.Bereikbaarheid
import nl.rijksoverheid.moz.digitalebereikbaarheid.entity.BereikbaarheidEntity

object BereikbaarheidMapper {

    fun toDto(entity: BereikbaarheidEntity): Bereikbaarheid = Bereikbaarheid(
        ontvangerId = entity.ontvangerId,
        ontvangerIdType = entity.ontvangerIdType,
        digitaalBereikbaar = entity.digitaalBereikbaar,
        registratieDatum = entity.registratieDatum,
        intrekkingsDatum = entity.intrekkingsDatum
    )
}
