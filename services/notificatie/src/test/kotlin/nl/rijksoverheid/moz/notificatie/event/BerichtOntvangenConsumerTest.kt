package nl.rijksoverheid.moz.notificatie.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cloudevents.CloudEvent
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.moz.cloudevents.FbsCloudEventBuilder
import nl.rijksoverheid.moz.cloudevents.FbsEventTypes
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import nl.rijksoverheid.moz.notificatie.service.NotificatieService
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.core.JsonProcessingException
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.time.Instant
import java.util.UUID

class BerichtOntvangenConsumerTest {

    private val notificatieService = mockk<NotificatieService>()
    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

    private val consumer = BerichtOntvangenConsumer(notificatieService, objectMapper)

    private val source = URI.create("urn:nld:oin:00000001234567890000:systeem:test")

    @Test
    fun `verwerkt geldig bericht-ontvangen event`() {
        val bericht = Bericht(
            id = UUID.randomUUID(),
            afzenderOin = "00000001234567890000",
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "999999999",
            onderwerp = "Test",
            inhoud = "Test inhoud",
            status = BerichtStatus.NIEUW,
            aangemaaktOp = Instant.now()
        )
        val event = buildEvent(FbsEventTypes.BERICHT_ONTVANGEN, bericht)

        every { notificatieService.verwerkBerichtOntvangen(any()) } just Runs

        consumer.onBerichtOntvangen(event)

        verify(exactly = 1) { notificatieService.verwerkBerichtOntvangen(any()) }
    }

    @Test
    fun `negeert event met verkeerd type`() {
        val event = FbsCloudEventBuilder.build(
            source = source,
            type = "nl.rijksoverheid.moz.bericht.gelezen"
        )

        consumer.onBerichtOntvangen(event)

        verify(exactly = 0) { notificatieService.verwerkBerichtOntvangen(any()) }
    }

    @Test
    fun `negeert event zonder data`() {
        val event = FbsCloudEventBuilder.build(
            source = source,
            type = FbsEventTypes.BERICHT_ONTVANGEN
        )

        consumer.onBerichtOntvangen(event)

        verify(exactly = 0) { notificatieService.verwerkBerichtOntvangen(any()) }
    }

    @Test
    fun `gooit deserialisatiefout door voor DLQ routing`() {
        val event = FbsCloudEventBuilder.build(
            source = source,
            type = FbsEventTypes.BERICHT_ONTVANGEN,
            data = "ongeldige json".toByteArray()
        )

        assertThrows<JsonProcessingException> {
            consumer.onBerichtOntvangen(event)
        }

        verify(exactly = 0) { notificatieService.verwerkBerichtOntvangen(any()) }
    }

    @Test
    fun `laat service exception doorbubbelen voor DLQ`() {
        val bericht = Bericht(
            id = UUID.randomUUID(),
            afzenderOin = "00000001234567890000",
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = "999999999",
            onderwerp = "Test",
            inhoud = "Test inhoud",
            status = BerichtStatus.NIEUW,
            aangemaaktOp = Instant.now()
        )
        val event = buildEvent(FbsEventTypes.BERICHT_ONTVANGEN, bericht)

        every { notificatieService.verwerkBerichtOntvangen(any()) } throws RuntimeException("Service fout")

        assertThrows<RuntimeException> {
            consumer.onBerichtOntvangen(event)
        }
    }

    private fun buildEvent(type: String, bericht: Bericht): CloudEvent {
        val data = objectMapper.writeValueAsBytes(bericht)
        return FbsCloudEventBuilder.build(
            source = source,
            type = type,
            subject = bericht.id.toString(),
            data = data
        )
    }
}
