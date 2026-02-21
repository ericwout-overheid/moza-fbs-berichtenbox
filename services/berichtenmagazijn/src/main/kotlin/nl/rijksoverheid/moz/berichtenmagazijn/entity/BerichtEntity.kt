package nl.rijksoverheid.moz.berichtenmagazijn.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "berichten")
class BerichtEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "afzender_oin", nullable = false, length = 20)
    var afzenderOin: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "ontvanger_id_type", nullable = false, length = 4)
    var ontvangerIdType: OntvangerIdType = OntvangerIdType.BSN,

    @Column(name = "ontvanger_id", nullable = false, length = 20)
    var ontvangerId: String = "",

    @Column(nullable = false, length = 500)
    var onderwerp: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var inhoud: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    var status: BerichtStatus = BerichtStatus.NIEUW,

    @Column(name = "aangemaakt_op", nullable = false)
    var aangemaaktOp: Instant = Instant.now(),

    @Column(name = "gelezen_op")
    var gelezenOp: Instant? = null,

    @OneToMany(mappedBy = "bericht", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bijlagen: MutableList<BijlageEntity> = mutableListOf()
) {
    protected constructor() : this(id = UUID.randomUUID())
}
