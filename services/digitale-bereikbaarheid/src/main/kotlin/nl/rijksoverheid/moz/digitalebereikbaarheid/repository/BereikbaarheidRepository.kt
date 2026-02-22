package nl.rijksoverheid.moz.digitalebereikbaarheid.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.digitalebereikbaarheid.entity.BereikbaarheidEntity
import java.util.UUID

@ApplicationScoped
class BereikbaarheidRepository : PanacheRepositoryBase<BereikbaarheidEntity, UUID> {

    fun vindOpOntvanger(ontvangerId: String, ontvangerIdType: OntvangerIdType): BereikbaarheidEntity? =
        find("ontvangerId = ?1 and ontvangerIdType = ?2", ontvangerId, ontvangerIdType)
            .firstResult()

    fun bewaar(entity: BereikbaarheidEntity) = persist(entity)
}
