package nl.rijksoverheid.moz.notificatieprofiel.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import nl.rijksoverheid.moz.common.model.NotificatieFrequentie
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Profiel
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "profielen")
class ProfielEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "ontvanger_id", nullable = false, length = 20)
    val ontvangerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "ontvanger_id_type", nullable = false, length = 10)
    val ontvangerIdType: OntvangerIdType = OntvangerIdType.BSN,

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
    val aangemaaktOp: Instant = Instant.now(),

    @Column(name = "bijgewerkt_op", nullable = false)
    var bijgewerktOp: Instant = Instant.now()
) {
    // No-arg constructor vereist door Hibernate. Default waarden in de primaire constructor
    // dienen alleen Hibernate; validatie vindt plaats in de service-laag.
    protected constructor() : this(id = UUID.randomUUID())

    fun werkBij(profiel: Profiel) {
        emailNotificaties = profiel.emailNotificaties
        smsNotificaties = profiel.smsNotificaties
        emailAdres = profiel.emailAdres
        telefoonnummer = profiel.telefoonnummer
        frequentie = profiel.frequentie
        bijgewerktOp = Instant.now()
    }
}
