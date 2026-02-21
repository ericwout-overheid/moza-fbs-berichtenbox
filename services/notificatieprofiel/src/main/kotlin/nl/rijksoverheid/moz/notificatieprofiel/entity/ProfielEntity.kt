package nl.rijksoverheid.moz.notificatieprofiel.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import nl.rijksoverheid.moz.common.model.NotificatieFrequentie
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "profielen")
class ProfielEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "ontvanger_id", nullable = false, length = 20)
    var ontvangerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "ontvanger_id_type", nullable = false, length = 4)
    var ontvangerIdType: OntvangerIdType = OntvangerIdType.BSN,

    @Column(name = "email_notificaties", nullable = false)
    var emailNotificaties: Boolean = false,

    @Column(name = "sms_notificaties", nullable = false)
    var smsNotificaties: Boolean = false,

    @Column(name = "email_adres", length = 254)
    var emailAdres: String? = null,

    @Column(length = 20)
    var telefoonnummer: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var frequentie: NotificatieFrequentie = NotificatieFrequentie.DIRECT,

    @Column(name = "aangemaakt_op", nullable = false)
    var aangemaaktOp: Instant = Instant.now(),

    @Column(name = "bijgewerkt_op", nullable = false)
    var bijgewerktOp: Instant = Instant.now()
) {
    protected constructor() : this(id = UUID.randomUUID())
}
