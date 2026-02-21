package nl.rijksoverheid.moz.common.model

import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NotificatieStatusTest {

    private val notificatieId = UUID.randomUUID()

    @Test
    fun `AANGEMAAKT zonder foutmelding is geldig`() {
        NotificatieStatus(
            notificatieId = notificatieId,
            status = NotificatieStatusWaarde.AANGEMAAKT
        )
    }

    @Test
    fun `VERZONDEN zonder foutmelding is geldig`() {
        NotificatieStatus(
            notificatieId = notificatieId,
            status = NotificatieStatusWaarde.VERZONDEN,
            verzondenOp = Instant.now()
        )
    }

    @Test
    fun `AFGELEVERD zonder foutmelding is geldig`() {
        NotificatieStatus(
            notificatieId = notificatieId,
            status = NotificatieStatusWaarde.AFGELEVERD,
            verzondenOp = Instant.now(),
            afgeleverdOp = Instant.now()
        )
    }

    @Test
    fun `MISLUKT met foutmelding is geldig`() {
        NotificatieStatus(
            notificatieId = notificatieId,
            status = NotificatieStatusWaarde.MISLUKT,
            foutmelding = "SMTP server onbereikbaar"
        )
    }

    @Test
    fun `MISLUKT zonder foutmelding gooit exception`() {
        assertFailsWith<IllegalArgumentException> {
            NotificatieStatus(
                notificatieId = notificatieId,
                status = NotificatieStatusWaarde.MISLUKT
            )
        }
    }

    @Test
    fun `MISLUKT met lege foutmelding gooit exception`() {
        assertFailsWith<IllegalArgumentException> {
            NotificatieStatus(
                notificatieId = notificatieId,
                status = NotificatieStatusWaarde.MISLUKT,
                foutmelding = "   "
            )
        }
    }

    @Test
    fun `AANGEMAAKT met foutmelding gooit exception`() {
        assertFailsWith<IllegalArgumentException> {
            NotificatieStatus(
                notificatieId = notificatieId,
                status = NotificatieStatusWaarde.AANGEMAAKT,
                foutmelding = "zou niet mogen"
            )
        }
    }

    @Test
    fun `VERZONDEN met foutmelding gooit exception`() {
        assertFailsWith<IllegalArgumentException> {
            NotificatieStatus(
                notificatieId = notificatieId,
                status = NotificatieStatusWaarde.VERZONDEN,
                foutmelding = "zou niet mogen"
            )
        }
    }
}
