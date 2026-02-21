package nl.rijksoverheid.moz.ldv

import io.opentelemetry.sdk.OpenTelemetrySdk
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LdvConfigurationTest {

    @Test
    fun `create geeft een geconfigureerde OpenTelemetrySdk`() {
        val otel = LdvConfiguration.create()
        assertIs<OpenTelemetrySdk>(otel)
        otel.close()
    }

    @Test
    fun `sampler is altijd AlwaysOnSampler`() {
        val otel = LdvConfiguration.create()
        val tracerProvider = otel.sdkTracerProvider
        assertEquals("AlwaysOnSampler", tracerProvider.sampler.description)
        otel.close()
    }

    @Test
    fun `leeg endpoint gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvConfiguration.create("")
        }
    }

    @Test
    fun `ongeldig endpoint zonder http schema gooit exception`() {
        assertThrows<IllegalArgumentException> {
            LdvConfiguration.create("invalid-endpoint")
        }
    }

    @Test
    fun `https endpoint is geldig`() {
        val otel = LdvConfiguration.create("https://collector.example.com:4317")
        assertIs<OpenTelemetrySdk>(otel)
        otel.close()
    }
}
