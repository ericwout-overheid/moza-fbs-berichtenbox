package nl.rijksoverheid.moz.berichtenmagazijn.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BerichtEntity
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BijlageEntity
import nl.rijksoverheid.moz.berichtenmagazijn.exception.BerichtNietGevondenException
import nl.rijksoverheid.moz.berichtenmagazijn.mapping.BerichtMapper
import nl.rijksoverheid.moz.berichtenmagazijn.repository.BerichtRepository
import nl.rijksoverheid.moz.berichtenmagazijn.repository.BijlageRepository
import nl.rijksoverheid.moz.berichtenmagazijn.storage.MinioStorageService
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtAanmaakVerzoek
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.BerichtStatusWijziging
import nl.rijksoverheid.moz.common.model.BijlageMetadata
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.common.model.Page
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.ldv.LdvLogger
import org.jboss.logging.Logger
import nl.rijksoverheid.moz.ldv.LdvVerwerking
import java.io.InputStream
import java.net.URI
import java.time.Instant
import java.util.UUID

@ApplicationScoped
@Transactional
class BerichtService(
    private val berichtRepository: BerichtRepository,
    private val bijlageRepository: BijlageRepository,
    private val storageService: MinioStorageService,
    private val ldvLogger: LdvLogger
) {

    private val log = Logger.getLogger(BerichtService::class.java)

    fun maakBericht(afzenderOin: String, verzoek: BerichtAanmaakVerzoek): Bericht {
        val entity = BerichtEntity(
            afzenderOin = afzenderOin,
            ontvangerIdType = verzoek.ontvangerIdType,
            ontvangerId = verzoek.ontvangerId,
            onderwerp = verzoek.onderwerp,
            inhoud = verzoek.inhoud
        )

        return ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bericht-opslaan"),
                betrokkeneId = verzoek.ontvangerId,
                betrokkeneIdType = verzoek.ontvangerIdType.name,
                operatieNaam = "maakBericht"
            )
        ) {
            berichtRepository.bewaar(entity)
            BerichtMapper.toDto(entity)
        }
    }

    fun haalBericht(berichtId: UUID): Bericht {
        val entity = berichtRepository.vindOpId(berichtId)
            ?: throw BerichtNietGevondenException(berichtId)

        ldvLogger.logVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bericht-ophalen"),
                betrokkeneId = entity.ontvangerId,
                betrokkeneIdType = entity.ontvangerIdType.name,
                operatieNaam = "haalBericht"
            )
        )

        return BerichtMapper.toDto(entity)
    }

    fun lijstBerichten(
        ontvangerIdType: OntvangerIdType?,
        ontvangerId: String?,
        status: BerichtStatus?,
        page: Int,
        pageSize: Int
    ): Page<Bericht> {
        require((ontvangerIdType == null) == (ontvangerId == null)) {
            "ontvangerIdType en ontvangerId moeten samen opgegeven worden"
        }
        require(page >= 1) { "page moet >= 1 zijn" }
        require(pageSize >= 1) { "pageSize moet >= 1 zijn" }

        val effectivePageSize = pageSize.coerceAtMost(FbsConstants.MAX_PAGE_SIZE)
        // API uses 1-based pages (per OpenAPI spec), Panache uses 0-based
        val zeroBasedPage = page - 1

        if (ontvangerIdType != null && ontvangerId != null) {
            val entities: List<BerichtEntity>
            val total: Long

            if (status != null) {
                entities = berichtRepository.findByOntvangerAndStatus(
                    ontvangerIdType, ontvangerId, status, zeroBasedPage, effectivePageSize
                )
                total = berichtRepository.countByOntvangerAndStatus(
                    ontvangerIdType, ontvangerId, status
                )
            } else {
                entities = berichtRepository.findByOntvanger(
                    ontvangerIdType, ontvangerId, zeroBasedPage, effectivePageSize
                )
                total = berichtRepository.countByOntvanger(ontvangerIdType, ontvangerId)
            }

            return Page(
                resultaten = entities.map { BerichtMapper.toDto(it) },
                pagina = page,
                paginaGrootte = effectivePageSize,
                totaalPaginas = Page.berekenTotaalPaginas(total, effectivePageSize),
                totaalElementen = total
            )
        }

        val total = berichtRepository.telAlles()
        val entities = berichtRepository.vindAlles()
            .page(zeroBasedPage, effectivePageSize)
            .list()

        return Page(
            resultaten = entities.map { BerichtMapper.toDto(it) },
            pagina = page,
            paginaGrootte = effectivePageSize,
            totaalPaginas = Page.berekenTotaalPaginas(total, effectivePageSize),
            totaalElementen = total
        )
    }

    fun werkBerichtBij(berichtId: UUID, wijziging: BerichtStatusWijziging): Bericht {
        val entity = berichtRepository.vindOpId(berichtId)
            ?: throw BerichtNietGevondenException(berichtId)

        return ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bericht-bijwerken"),
                betrokkeneId = entity.ontvangerId,
                betrokkeneIdType = entity.ontvangerIdType.name,
                operatieNaam = "werkBerichtBij"
            )
        ) {
            entity.status = wijziging.status
            if (wijziging.status == BerichtStatus.GELEZEN && entity.gelezenOp == null) {
                entity.gelezenOp = Instant.now()
            }
            berichtRepository.bewaar(entity)
            BerichtMapper.toDto(entity)
        }
    }

    fun verwijderBericht(berichtId: UUID): String {
        val entity = berichtRepository.vindOpId(berichtId)
            ?: throw BerichtNietGevondenException(berichtId)

        val afzenderOin = entity.afzenderOin
        val objectKeys = entity.bijlagen.map { it.objectKey }

        ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bericht-verwijderen"),
                betrokkeneId = entity.ontvangerId,
                betrokkeneIdType = entity.ontvangerIdType.name,
                operatieNaam = "verwijderBericht"
            )
        ) {
            berichtRepository.verwijderOpId(berichtId)
        }

        // MinIO cleanup buiten transactie (best-effort)
        objectKeys.forEach { objectKey ->
            try {
                storageService.delete(objectKey)
            } catch (e: Exception) {
                log.errorf(e, "MinIO object verwijderen mislukt: objectKey=%s, berichtId=%s",
                    objectKey, berichtId)
            }
        }

        return afzenderOin
    }

    fun uploadBijlage(
        berichtId: UUID,
        bestandsnaam: String,
        mediaType: String,
        inputStream: InputStream,
        grootte: Long
    ): BijlageMetadata {
        val bericht = berichtRepository.vindOpId(berichtId)
            ?: throw BerichtNietGevondenException(berichtId)

        val bijlageId = UUID.randomUUID()
        val objectKey = MinioStorageService.objectKey(berichtId, bijlageId, bestandsnaam)

        storageService.upload(objectKey, inputStream, mediaType, grootte)

        try {
            val bijlage = BijlageEntity(
                id = bijlageId,
                bericht = bericht,
                bestandsnaam = bestandsnaam,
                mediaType = mediaType,
                grootte = grootte,
                objectKey = objectKey
            )
            bijlageRepository.bewaar(bijlage)
            return BerichtMapper.toBijlageDto(bijlage)
        } catch (e: Exception) {
            log.errorf(e, "Database persist mislukt na MinIO upload, opruimen: objectKey=%s", objectKey)
            try {
                storageService.delete(objectKey)
            } catch (cleanupEx: Exception) {
                log.errorf(cleanupEx, "Compenserende MinIO delete mislukt, orphaned object: objectKey=%s", objectKey)
            }
            throw e
        }
    }

    fun lijstBijlagen(berichtId: UUID): List<BijlageMetadata> {
        berichtRepository.vindOpId(berichtId)
            ?: throw BerichtNietGevondenException(berichtId)

        return bijlageRepository.findByBerichtId(berichtId)
            .map { BerichtMapper.toBijlageDto(it) }
    }
}
