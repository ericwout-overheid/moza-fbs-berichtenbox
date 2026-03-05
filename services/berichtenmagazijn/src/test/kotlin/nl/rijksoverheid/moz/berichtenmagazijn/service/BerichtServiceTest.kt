package nl.rijksoverheid.moz.berichtenmagazijn.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BerichtEntity
import nl.rijksoverheid.moz.berichtenmagazijn.entity.BijlageEntity
import nl.rijksoverheid.moz.berichtenmagazijn.exception.BerichtNietGevondenException
import nl.rijksoverheid.moz.berichtenmagazijn.repository.BerichtRepository
import nl.rijksoverheid.moz.berichtenmagazijn.repository.BijlageRepository
import nl.rijksoverheid.moz.berichtenmagazijn.storage.MinioStorageService
import nl.rijksoverheid.moz.common.model.BerichtAanmaakVerzoek
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.BerichtStatusWijziging
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.ldv.LdvLogger
import nl.rijksoverheid.moz.ldv.LdvPseudonimiseerder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.UUID

class BerichtServiceTest {

    private val berichtRepository = mockk<BerichtRepository>()
    private val bijlageRepository = mockk<BijlageRepository>()
    private val storageService = mockk<MinioStorageService>()
    private val ldvLogger = mockk<LdvLogger>()
    private val pseudonimiseerder = LdvPseudonimiseerder.create("test-zout-minimaal-32-tekens-lang!")

    private val service = BerichtService(
        berichtRepository, bijlageRepository, storageService, ldvLogger, pseudonimiseerder
    )

    private val testOin = "00000001234567890000"

    init {
        every { ldvLogger.logVerwerking(any()) } just Runs
        every { ldvLogger.withinVerwerking(any(), any<() -> Any>()) } answers {
            val block = secondArg<() -> Any>()
            block()
        }
    }

    @Test
    fun `maakBericht creates bericht`() {
        val verzoek = BerichtAanmaakVerzoek(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "999999999",
            onderwerp = "Test onderwerp",
            inhoud = "Test inhoud"
        )

        every { berichtRepository.save(any<BerichtEntity>()) } just Runs

        val bericht = service.maakBericht(testOin, verzoek)

        assertEquals(testOin, bericht.afzenderOin)
        assertEquals(OntvangerIdType.BSN, bericht.ontvangerIdType)
        assertEquals("999999999", bericht.ontvangerId)
        assertEquals("Test onderwerp", bericht.onderwerp)
        assertEquals(BerichtStatus.NIEUW, bericht.status)
        assertNull(bericht.gelezenOp)
    }

    @Test
    fun `maakBericht rejects empty ontvangerId`() {
        assertThrows<IllegalArgumentException> {
            val verzoek = BerichtAanmaakVerzoek(
                ontvangerIdType = OntvangerIdType.BSN,
                ontvangerId = "",
                onderwerp = "Test onderwerp",
                inhoud = "Test inhoud"
            )
            service.maakBericht(testOin, verzoek)
        }
    }

    @Test
    fun `haalBericht returns bericht when found`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)

        every { berichtRepository.getById(berichtId) } returns entity

        val bericht = service.haalBericht(berichtId)

        assertEquals(berichtId, bericht.id)
        assertEquals(testOin, bericht.afzenderOin)
    }

    @Test
    fun `haalBericht throws exception when not found`() {
        val berichtId = UUID.randomUUID()
        every { berichtRepository.getById(berichtId) } returns null

        assertThrows<BerichtNietGevondenException> {
            service.haalBericht(berichtId)
        }
    }

    @Test
    fun `lijstBerichten returns paginated results with filters`() {
        val entity = createTestEntity()
        every {
            berichtRepository.findByOntvangerAndStatus(
                OntvangerIdType.BSN, "999999999", BerichtStatus.NIEUW, 0, 20
            )
        } returns listOf(entity)
        every {
            berichtRepository.countByOntvangerAndStatus(
                OntvangerIdType.BSN, "999999999", BerichtStatus.NIEUW
            )
        } returns 1L

        val page = service.lijstBerichten(OntvangerIdType.BSN, "999999999", BerichtStatus.NIEUW, null, 1, 20)

        assertEquals(1, page.resultaten.size)
        assertEquals(1, page.pagina)
        assertEquals(20, page.paginaGrootte)
        assertEquals(1L, page.totaalElementen)
    }

    @Test
    fun `lijstBerichten returns paginated results without status filter`() {
        val entity = createTestEntity()
        every {
            berichtRepository.findByOntvanger(OntvangerIdType.BSN, "999999999", 0, 20)
        } returns listOf(entity)
        every {
            berichtRepository.countByOntvanger(OntvangerIdType.BSN, "999999999")
        } returns 1L

        val page = service.lijstBerichten(OntvangerIdType.BSN, "999999999", null, null, 1, 20)

        assertEquals(1, page.resultaten.size)
        assertEquals(1L, page.totaalElementen)
    }

    @Test
    fun `lijstBerichten returns all berichten without ontvanger filter`() {
        val entity = createTestEntity()
        val query = mockk<PanacheQuery<BerichtEntity>>()

        every { berichtRepository.countAll() } returns 1L
        every { berichtRepository.queryAll() } returns query
        every { query.page(0, 20) } returns query
        every { query.list() } returns listOf(entity)

        val page = service.lijstBerichten(null, null, null, null, 1, 20)

        assertEquals(1, page.resultaten.size)
        assertEquals(1L, page.totaalElementen)
    }

    @Test
    fun `werkBerichtBij sets gelezenOp when status becomes GELEZEN`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)

        every { berichtRepository.getById(berichtId) } returns entity
        every { berichtRepository.save(any<BerichtEntity>()) } just Runs

        val bericht = service.werkBerichtBij(berichtId, BerichtStatusWijziging(BerichtStatus.GELEZEN))

        assertEquals(BerichtStatus.GELEZEN, bericht.status)
        assertNotNull(bericht.gelezenOp)
    }

    @Test
    fun `verwijderBericht deletes MinIO objects and entity`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)
        val bijlage = BijlageEntity(
            bericht = entity,
            bestandsnaam = "test.pdf",
            mediaType = "application/pdf",
            grootte = 1024,
            objectKey = "berichten/$berichtId/bijlagen/test/test.pdf"
        )
        entity.bijlagen.add(bijlage)

        every { berichtRepository.getById(berichtId) } returns entity
        every { storageService.delete(any()) } just Runs
        every { berichtRepository.removeById(berichtId) } returns true

        val afzenderOin = service.verwijderBericht(berichtId)

        assertEquals(testOin, afzenderOin)
        verify { storageService.delete(bijlage.objectKey) }
        verify { berichtRepository.removeById(berichtId) }
    }

    @Test
    fun `uploadBijlage stores file and persists entity`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)
        val inputStream = ByteArrayInputStream("test data".toByteArray())

        every { berichtRepository.getById(berichtId) } returns entity
        every { storageService.upload(any(), any(), any(), any()) } just Runs
        every { bijlageRepository.save(any<BijlageEntity>()) } just Runs

        val bijlage = service.uploadBijlage(berichtId, "test.pdf", "application/pdf", inputStream, 9L)

        assertEquals("test.pdf", bijlage.bestandsnaam)
        assertEquals("application/pdf", bijlage.mediaType)
        assertEquals(9L, bijlage.grootte)

        verify { storageService.upload(any(), any(), eq("application/pdf"), eq(9L)) }
    }

    @Test
    fun `lijstBijlagen returns bijlagen for bericht`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)
        val bijlage = BijlageEntity(
            bericht = entity,
            bestandsnaam = "doc.pdf",
            mediaType = "application/pdf",
            grootte = 2048,
            objectKey = "berichten/$berichtId/bijlagen/test/doc.pdf"
        )

        every { berichtRepository.getById(berichtId) } returns entity
        every { bijlageRepository.findByBerichtId(berichtId) } returns listOf(bijlage)

        val result = service.lijstBijlagen(berichtId)

        assertEquals(1, result.size)
        assertEquals("doc.pdf", result[0].bestandsnaam)
    }

    @Test
    fun `werkBerichtBij does not overwrite gelezenOp when already set`() {
        val berichtId = UUID.randomUUID()
        val originalGelezenOp = Instant.parse("2026-01-15T10:00:00Z")
        val entity = createTestEntity(berichtId).apply {
            status = BerichtStatus.GELEZEN
            gelezenOp = originalGelezenOp
        }

        every { berichtRepository.getById(berichtId) } returns entity
        every { berichtRepository.save(any<BerichtEntity>()) } just Runs

        val bericht = service.werkBerichtBij(berichtId, BerichtStatusWijziging(BerichtStatus.GELEZEN))

        assertEquals(originalGelezenOp, bericht.gelezenOp)
    }

    @Test
    fun `verwijderBericht deletes all bijlagen from storage`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)
        val bijlage1 = BijlageEntity(
            bericht = entity, bestandsnaam = "a.pdf",
            mediaType = "application/pdf", grootte = 1024, objectKey = "key1"
        )
        val bijlage2 = BijlageEntity(
            bericht = entity, bestandsnaam = "b.pdf",
            mediaType = "application/pdf", grootte = 2048, objectKey = "key2"
        )
        entity.bijlagen.addAll(listOf(bijlage1, bijlage2))

        every { berichtRepository.getById(berichtId) } returns entity
        every { storageService.delete(any()) } just Runs
        every { berichtRepository.removeById(berichtId) } returns true

        service.verwijderBericht(berichtId)

        verify(exactly = 1) { storageService.delete("key1") }
        verify(exactly = 1) { storageService.delete("key2") }
    }

    @Test
    fun `uploadBijlage throws exception when bericht not found`() {
        val berichtId = UUID.randomUUID()
        every { berichtRepository.getById(berichtId) } returns null

        assertThrows<BerichtNietGevondenException> {
            service.uploadBijlage(
                berichtId, "test.pdf", "application/pdf",
                ByteArrayInputStream("data".toByteArray()), 4L
            )
        }

        verify(exactly = 0) { storageService.upload(any(), any(), any(), any()) }
    }

    @Test
    fun `lijstBijlagen throws exception when bericht not found`() {
        val berichtId = UUID.randomUUID()
        every { berichtRepository.getById(berichtId) } returns null

        assertThrows<BerichtNietGevondenException> {
            service.lijstBijlagen(berichtId)
        }
    }

    @Test
    fun `lijstBerichten rejects partial filter parameters`() {
        assertThrows<IllegalArgumentException> {
            service.lijstBerichten(OntvangerIdType.BSN, null, null, null, 1, 20)
        }
    }

    @Test
    fun `lijstBerichten rejects page less than 1`() {
        assertThrows<IllegalArgumentException> {
            service.lijstBerichten(null, null, null, null, 0, 20)
        }
    }

    @Test
    fun `lijstBerichten clamps pageSize to max`() {
        val entity = createTestEntity()
        val query = mockk<PanacheQuery<BerichtEntity>>()

        every { berichtRepository.countAll() } returns 1L
        every { berichtRepository.queryAll() } returns query
        every { query.page(0, 100) } returns query
        every { query.list() } returns listOf(entity)

        val page = service.lijstBerichten(null, null, null, null, 1, 500)

        assertEquals(100, page.paginaGrootte)
    }

    @Test
    fun `lijstBerichten rejects pageSize less than 1`() {
        assertThrows<IllegalArgumentException> {
            service.lijstBerichten(null, null, null, null, 1, 0)
        }
    }

    @Test
    fun `lijstBerichten rejects status filter without ontvanger`() {
        assertThrows<IllegalArgumentException> {
            service.lijstBerichten(null, null, BerichtStatus.NIEUW, null, 1, 20)
        }
    }

    @Test
    fun `werkBerichtBij throws exception when not found`() {
        val berichtId = UUID.randomUUID()
        every { berichtRepository.getById(berichtId) } returns null

        assertThrows<BerichtNietGevondenException> {
            service.werkBerichtBij(berichtId, BerichtStatusWijziging(BerichtStatus.GELEZEN))
        }
    }

    @Test
    fun `werkBerichtBij with GEARCHIVEERD does not set gelezenOp`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)

        every { berichtRepository.getById(berichtId) } returns entity
        every { berichtRepository.save(any<BerichtEntity>()) } just Runs

        val bericht = service.werkBerichtBij(berichtId, BerichtStatusWijziging(BerichtStatus.GEARCHIVEERD))

        assertEquals(BerichtStatus.GEARCHIVEERD, bericht.status)
        assertNull(bericht.gelezenOp)
    }

    @Test
    fun `verwijderBericht throws exception when not found`() {
        val berichtId = UUID.randomUUID()
        every { berichtRepository.getById(berichtId) } returns null

        assertThrows<BerichtNietGevondenException> {
            service.verwijderBericht(berichtId)
        }
    }

    @Test
    fun `verwijderBericht continues when MinIO delete fails`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)
        val bijlage1 = BijlageEntity(
            bericht = entity, bestandsnaam = "a.pdf",
            mediaType = "application/pdf", grootte = 1024, objectKey = "key1"
        )
        val bijlage2 = BijlageEntity(
            bericht = entity, bestandsnaam = "b.pdf",
            mediaType = "application/pdf", grootte = 2048, objectKey = "key2"
        )
        entity.bijlagen.addAll(listOf(bijlage1, bijlage2))

        every { berichtRepository.getById(berichtId) } returns entity
        every { berichtRepository.removeById(berichtId) } returns true
        every { storageService.delete("key1") } throws RuntimeException("MinIO down")
        every { storageService.delete("key2") } just Runs

        val afzenderOin = service.verwijderBericht(berichtId)

        assertEquals(testOin, afzenderOin)
        verify(exactly = 1) { storageService.delete("key1") }
        verify(exactly = 1) { storageService.delete("key2") }
    }

    @Test
    fun `uploadBijlage compensates MinIO on DB persist failure`() {
        val berichtId = UUID.randomUUID()
        val entity = createTestEntity(berichtId)

        every { berichtRepository.getById(berichtId) } returns entity
        every { storageService.upload(any(), any(), any(), any()) } just Runs
        every { bijlageRepository.save(any<BijlageEntity>()) } throws RuntimeException("DB error")
        every { storageService.delete(any()) } just Runs

        assertThrows<RuntimeException> {
            service.uploadBijlage(
                berichtId, "test.pdf", "application/pdf",
                ByteArrayInputStream("data".toByteArray()), 4L
            )
        }

        verify { storageService.delete(any()) }
    }

    private fun createTestEntity(id: UUID = UUID.randomUUID()) = BerichtEntity(
        id = id,
        afzenderOin = testOin,
        ontvangerIdType = OntvangerIdType.BSN,
        ontvangerId = "999999999",
        onderwerp = "Test onderwerp",
        inhoud = "Test inhoud",
        status = BerichtStatus.NIEUW,
        aangemaaktOp = Instant.now()
    )
}
