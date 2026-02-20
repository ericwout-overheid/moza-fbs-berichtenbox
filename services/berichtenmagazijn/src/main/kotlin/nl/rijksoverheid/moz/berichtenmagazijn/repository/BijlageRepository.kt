package nl.rijksoverheid.moz.berichtenmagazijn.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BijlageEntity
import java.util.UUID

@ApplicationScoped
class BijlageRepository : PanacheRepositoryBase<BijlageEntity, UUID> {

    fun bewaar(entity: BijlageEntity) = persist(entity)

    fun findByBerichtId(berichtId: UUID): List<BijlageEntity> =
        find("bericht.id", berichtId).list()
}
