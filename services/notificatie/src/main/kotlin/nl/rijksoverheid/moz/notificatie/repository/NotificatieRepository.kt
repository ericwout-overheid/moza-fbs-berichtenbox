package nl.rijksoverheid.moz.notificatie.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import java.util.UUID

@ApplicationScoped
class NotificatieRepository : PanacheRepositoryBase<NotificatieEntity, UUID> {

    fun vindOpId(id: UUID): NotificatieEntity? = findById(id)

    fun bewaar(entity: NotificatieEntity) = persist(entity)

    fun findRetryable(maxPogingen: Int): List<NotificatieEntity> =
        find("status = ?1 and pogingen < ?2", NotificatieStatusWaarde.MISLUKT, maxPogingen).list()
}
