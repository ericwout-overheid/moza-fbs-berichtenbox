package nl.rijksoverheid.moz.berichtenmagazijn.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BerichtEntity
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import java.util.UUID

@ApplicationScoped
class BerichtRepository : PanacheRepositoryBase<BerichtEntity, UUID> {

    fun getById(id: UUID): BerichtEntity? = findById(id)

    fun save(entity: BerichtEntity) = persist(entity)

    fun removeById(id: UUID): Boolean = deleteById(id)

    fun countAll(): Long = count()

    fun queryAll(): PanacheQuery<BerichtEntity> = findAll()

    fun findByOntvanger(idType: OntvangerIdType, id: String, page: Int, size: Int): List<BerichtEntity> =
        find("ontvangerIdType = ?1 and ontvangerId = ?2 order by aangemaaktOp desc", idType, id)
            .page(page, size)
            .list()

    fun findByOntvangerAndStatus(
        idType: OntvangerIdType,
        id: String,
        status: BerichtStatus,
        page: Int,
        size: Int
    ): List<BerichtEntity> =
        find(
            "ontvangerIdType = ?1 and ontvangerId = ?2 and status = ?3 order by aangemaaktOp desc",
            idType, id, status
        ).page(page, size).list()

    fun countByOntvanger(idType: OntvangerIdType, id: String): Long =
        count("ontvangerIdType = ?1 and ontvangerId = ?2", idType, id)

    fun countByOntvangerAndStatus(idType: OntvangerIdType, id: String, status: BerichtStatus): Long =
        count("ontvangerIdType = ?1 and ontvangerId = ?2 and status = ?3", idType, id, status)

    fun findByOntvangerAndOnderwerp(
        idType: OntvangerIdType,
        id: String,
        onderwerp: String,
        page: Int,
        size: Int
    ): List<BerichtEntity> =
        find(
            "ontvangerIdType = ?1 and ontvangerId = ?2 and lower(onderwerp) like lower(?3) order by aangemaaktOp desc",
            idType, id, "%$onderwerp%"
        ).page(page, size).list()

    fun countByOntvangerAndOnderwerp(idType: OntvangerIdType, id: String, onderwerp: String): Long =
        count(
            "ontvangerIdType = ?1 and ontvangerId = ?2 and lower(onderwerp) like lower(?3)",
            idType, id, "%$onderwerp%"
        )
}
