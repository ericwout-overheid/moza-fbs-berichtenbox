package nl.rijksoverheid.moz.notificatie.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.enterprise.inject.Instance
import nl.rijksoverheid.moz.common.model.NotificatieKanaal
import nl.rijksoverheid.moz.common.model.NotificatieStatusWaarde
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.notificatie.entity.NotificatieEntity
import nl.rijksoverheid.moz.notificatie.repository.NotificatieRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.stream.Stream

class NotificatieRetryServiceTest {

    private val notificatieRepository = mockk<NotificatieRepository>(relaxed = true)
    private val verzenders = mockk<Instance<NotificatieVerzender>>()
    private val emailVerzender = mockk<NotificatieVerzender>()

    private lateinit var retryService: NotificatieRetryService

    @BeforeEach
    fun setup() {
        retryService = NotificatieRetryService(notificatieRepository, verzenders)
        every { emailVerzender.kanaal } returns NotificatieKanaal.EMAIL
        every { verzenders.stream() } returns Stream.of(emailVerzender)
    }

    @Test
    fun `doet niets als er geen retryable notificaties zijn`() {
        every { notificatieRepository.findRetryable(any()) } returns emptyList()

        retryService.retryMislukteNotificaties()

        verify(exactly = 0) { notificatieRepository.bewaar(any()) }
    }

    @Test
    fun `succesvolle retry markeert als VERZONDEN`() {
        val entity = createMislukteEntity(pogingen = 1)
        every { notificatieRepository.findRetryable(any()) } returns listOf(entity)
        every { emailVerzender.verzend(any(), any(), any()) } returns Unit

        retryService.retryMislukteNotificaties()

        assertEquals(NotificatieStatusWaarde.VERZONDEN, entity.status)
        verify { notificatieRepository.bewaar(entity) }
    }

    @Test
    fun `mislukte retry onder max pogingen markeert als MISLUKT`() {
        val entity = createMislukteEntity(pogingen = 1)
        every { notificatieRepository.findRetryable(any()) } returns listOf(entity)
        every { emailVerzender.verzend(any(), any(), any()) } throws RuntimeException("Fout")

        retryService.retryMislukteNotificaties()

        assertEquals(NotificatieStatusWaarde.MISLUKT, entity.status)
        assertEquals(2, entity.pogingen)
        verify { notificatieRepository.bewaar(entity) }
    }

    @Test
    fun `mislukte retry op max pogingen markeert als DEFINITIEF_MISLUKT`() {
        val entity = createMislukteEntity(pogingen = 2)
        every { notificatieRepository.findRetryable(any()) } returns listOf(entity)
        every { emailVerzender.verzend(any(), any(), any()) } throws RuntimeException("Fout")

        retryService.retryMislukteNotificaties()

        assertEquals(NotificatieStatusWaarde.DEFINITIEF_MISLUKT, entity.status)
        verify { notificatieRepository.bewaar(entity) }
    }

    private fun createMislukteEntity(pogingen: Int): NotificatieEntity {
        val entity = NotificatieEntity(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "123456789",
            kanaal = NotificatieKanaal.EMAIL,
            onderwerp = "Test",
            inhoud = "Test inhoud",
            status = NotificatieStatusWaarde.MISLUKT,
            pogingen = pogingen
        )
        return entity
    }
}
