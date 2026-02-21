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
    var id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "ontvanger_id_type", nullable = false, length = 4)
    var ontvangerIdType: OntvangerIdType = OntvangerIdType.BSN,

    @Column(name = "ontvanger_id", nullable = false, length = 20)
    var ontvangerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    var kanaal: NotificatieKanaal = NotificatieKanaal.EMAIL,

    @Column(nullable = false, length = 200)
    var onderwerp: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var inhoud: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: NotificatieStatusWaarde = NotificatieStatusWaarde.AANGEMAAKT,

    @Column(name = "aangemaakt_op", nullable = false)
    var aangemaaktOp: Instant = Instant.now(),

    @Column(name = "verzonden_op")
    var verzondenOp: Instant? = null,

    @Column(name = "afgeleverd_op")
    var afgeleverdOp: Instant? = null,

    @Column(columnDefinition = "TEXT")
    var foutmelding: String? = null
) {
    protected constructor() : this(id = UUID.randomUUID())
}
