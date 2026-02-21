package nl.rijksoverheid.moz.notificatie.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import java.util.UUID

@ApplicationScoped
class NotificatieRepository : PanacheRepositoryBase<NotificatieEntity, UUID> {

    fun vindOpId(id: UUID): NotificatieEntity? = findById(id)

    fun bewaar(entity: NotificatieEntity) = persist(entity)
}
