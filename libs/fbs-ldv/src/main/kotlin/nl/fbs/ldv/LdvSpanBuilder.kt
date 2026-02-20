package nl.fbs.ldv

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer

/**
 * Builder voor OpenTelemetry spans met LDV-specifieke attributen.
 *
 * Voegt de verplichte `dpl.core.*` attributen toe aan spans conform
 * de Logboek Dataverwerkingen specificatie.
 */
class LdvSpanBuilder(private val tracer: Tracer) {

    /**
     * Maakt een nieuwe LDV span aan met de verplichte DPL attributen.
     *
     * @param verwerking de dataverwerking die wordt gelogd
     * @return de gestarte [Span]
     */
    fun startSpan(verwerking: LdvVerwerking): Span {
        return tracer.spanBuilder(verwerking.operatieNaam)
            .setAttribute(PROCESSING_ACTIVITY_ID, verwerking.verwerkingsActiviteitId.toString())
            .setAttribute(DATA_SUBJECT_ID, verwerking.betrokkeneId)
            .setAttribute(DATA_SUBJECT_ID_TYPE, verwerking.betrokkeneIdType)
            .startSpan()
    }

    companion object {
        private val PROCESSING_ACTIVITY_ID = AttributeKey.stringKey("dpl.core.processing_activity_id")
        private val DATA_SUBJECT_ID = AttributeKey.stringKey("dpl.core.data_subject_id")
        private val DATA_SUBJECT_ID_TYPE = AttributeKey.stringKey("dpl.core.data_subject_id_type")

        private const val INSTRUMENTATION_NAME = "nl.fbs.ldv"

        /**
         * Maakt een [LdvSpanBuilder] aan met de opgegeven [OpenTelemetry] instantie.
         */
        fun create(openTelemetry: OpenTelemetry): LdvSpanBuilder {
            return LdvSpanBuilder(openTelemetry.getTracer(INSTRUMENTATION_NAME))
        }
    }
}
