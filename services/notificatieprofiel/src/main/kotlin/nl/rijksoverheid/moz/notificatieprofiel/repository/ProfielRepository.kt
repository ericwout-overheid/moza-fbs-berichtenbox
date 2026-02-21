package nl.rijksoverheid.moz.notificatieprofiel.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.notificatieprofiel.entity.ProfielEntity
import java.util.UUID

@ApplicationScoped
class ProfielRepository : PanacheRepositoryBase<ProfielEntity, UUID> {

    fun vindOpOntvanger(ontvangerId: String, ontvangerIdType: OntvangerIdType): ProfielEntity? =
        find("ontvangerId = ?1 and ontvangerIdType = ?2", ontvangerId, ontvangerIdType)
            .firstResult()

    fun bewaar(entity: ProfielEntity) = persist(entity)
}
