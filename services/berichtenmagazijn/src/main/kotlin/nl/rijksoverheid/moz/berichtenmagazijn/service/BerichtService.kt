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
import nl.rijksoverheid.moz.ldv.LdvLogger
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
        val zeroBasedPage = page - 1

        if (ontvangerIdType != null && ontvangerId != null) {
            val entities: List<BerichtEntity>
            val total: Long

            if (status != null) {
                entities = berichtRepository.findByOntvangerAndStatus(
                    ontvangerIdType, ontvangerId, status, zeroBasedPage, pageSize
                )
                total = berichtRepository.countByOntvangerAndStatus(
                    ontvangerIdType, ontvangerId, status
                )
            } else {
                entities = berichtRepository.findByOntvanger(
                    ontvangerIdType, ontvangerId, zeroBasedPage, pageSize
                )
                total = berichtRepository.countByOntvanger(ontvangerIdType, ontvangerId)
            }

            return Page(
                resultaten = entities.map { BerichtMapper.toDto(it) },
                pagina = page,
                paginaGrootte = pageSize,
                totaalPaginas = Page.berekenTotaalPaginas(total, pageSize),
                totaalElementen = total
            )
        }

        val total = berichtRepository.telAlles()
        val entities = berichtRepository.vindAlles()
            .page(zeroBasedPage, pageSize)
            .list()

        return Page(
            resultaten = entities.map { BerichtMapper.toDto(it) },
            pagina = page,
            paginaGrootte = pageSize,
            totaalPaginas = Page.berekenTotaalPaginas(total, pageSize),
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

        ldvLogger.withinVerwerking(
            LdvVerwerking(
                verwerkingsActiviteitId = URI("https://fbs.nl/verwerkingen/bericht-verwijderen"),
                betrokkeneId = entity.ontvangerId,
                betrokkeneIdType = entity.ontvangerIdType.name,
                operatieNaam = "verwijderBericht"
            )
        ) {
            entity.bijlagen.forEach { bijlage ->
                storageService.delete(bijlage.objectKey)
            }
            berichtRepository.verwijderOpId(berichtId)
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
    }

    fun lijstBijlagen(berichtId: UUID): List<BijlageMetadata> {
        berichtRepository.vindOpId(berichtId)
            ?: throw BerichtNietGevondenException(berichtId)

        return bijlageRepository.findByBerichtId(berichtId)
            .map { BerichtMapper.toBijlageDto(it) }
    }
}
