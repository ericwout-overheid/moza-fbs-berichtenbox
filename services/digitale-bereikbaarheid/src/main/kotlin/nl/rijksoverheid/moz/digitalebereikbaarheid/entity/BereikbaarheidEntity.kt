package nl.rijksoverheid.moz.digitalebereikbaarheid.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "bereikbaarheid")
class BereikbaarheidEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "ontvanger_id", nullable = false, length = 20)
    val ontvangerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "ontvanger_id_type", nullable = false, length = 10)
    val ontvangerIdType: OntvangerIdType = OntvangerIdType.BSN,

    @Column(name = "digitaal_bereikbaar", nullable = false)
    var digitaalBereikbaar: Boolean = false,

    @Column(name = "registratie_datum", nullable = false)
    val registratieDatum: Instant = Instant.now(),

    @Column(name = "intrekkings_datum")
    var intrekkingsDatum: Instant? = null
) {
    protected constructor() : this(id = UUID.randomUUID())
}
