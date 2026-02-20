package nl.fbs.ldv

import io.opentelemetry.sdk.OpenTelemetrySdk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

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
        assertNotNull(tracerProvider)
        // AlwaysOnSampler description is "AlwaysOnSampler"
        assertEquals("AlwaysOnSampler", tracerProvider.sampler.description)
        otel.close()
    }
}
