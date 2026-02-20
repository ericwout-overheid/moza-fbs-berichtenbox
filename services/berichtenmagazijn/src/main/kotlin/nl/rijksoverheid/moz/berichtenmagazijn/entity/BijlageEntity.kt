package nl.rijksoverheid.moz.berichtenmagazijn.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "bijlagen")
class BijlageEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "bericht_id", nullable = false)
    var bericht: BerichtEntity? = null,

    @Column(nullable = false, length = 255)
    var bestandsnaam: String = "",

    @Column(name = "media_type", nullable = false, length = 255)
    var mediaType: String = "",

    @Column(nullable = false)
    var grootte: Long = 0,

    @Column(name = "object_key", nullable = false, length = 512)
    var objectKey: String = "",

    @Column(name = "aangemaakt_op", nullable = false)
    var aangemaaktOp: Instant = Instant.now()
) {
    protected constructor() : this(id = UUID.randomUUID())
}
