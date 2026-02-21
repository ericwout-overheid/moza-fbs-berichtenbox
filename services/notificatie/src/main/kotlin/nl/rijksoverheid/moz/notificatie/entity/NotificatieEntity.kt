package nl.rijksoverheid.moz.notificatie.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notificaties")
class NotificatieEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "ontvanger_id_type", nullable = false, length = 4)
    val ontvangerIdType: OntvangerIdType = OntvangerIdType.BSN,

    @Column(name = "ontvanger_id", nullable = false, length = 20)
    val ontvangerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    val kanaal: NotificatieKanaal = NotificatieKanaal.EMAIL,

    @Column(nullable = false, length = 200)
    val onderwerp: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val inhoud: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: NotificatieStatusWaarde = NotificatieStatusWaarde.AANGEMAAKT,

    @Column(name = "aangemaakt_op", nullable = false)
    val aangemaaktOp: Instant = Instant.now(),

    @Column(name = "verzonden_op")
    var verzondenOp: Instant? = null,

    @Column(name = "afgeleverd_op")
    var afgeleverdOp: Instant? = null,

    @Column(columnDefinition = "TEXT")
    var foutmelding: String? = null
) {
    protected constructor() : this(id = UUID.randomUUID())

    fun markeerVerzonden() {
        check(status == NotificatieStatusWaarde.AANGEMAAKT) {
            "Kan alleen AANGEMAAKT notificaties markeren als VERZONDEN, huidige status: $status"
        }
        status = NotificatieStatusWaarde.VERZONDEN
        verzondenOp = Instant.now()
    }

    fun markeerMislukt(foutmelding: String) {
        check(status == NotificatieStatusWaarde.AANGEMAAKT) {
            "Kan alleen AANGEMAAKT notificaties markeren als MISLUKT, huidige status: $status"
        }
        status = NotificatieStatusWaarde.MISLUKT
        this.foutmelding = foutmelding
    }
}
