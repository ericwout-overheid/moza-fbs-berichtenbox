package nl.fbs.ldv

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.StatusCode

/**
 * Hoofdklasse voor het loggen van dataverwerkingen conform het Logboek Dataverwerkingen.
 *
 * Biedt een eenvoudige API voor het loggen van dataverwerkingen met automatisch
 * span lifecycle management.
 */
class LdvLogger(private val spanBuilder: LdvSpanBuilder) {

    /**
     * Logt een dataverwerking als een punt-gebeurtenis (span met directe start en einde).
     *
     * Gebruik [withinVerwerking] als de verwerking een codeblok omvat.
     *
     * @param verwerking de te loggen dataverwerking
     */
    fun logVerwerking(verwerking: LdvVerwerking) {
        val span = spanBuilder.startSpan(verwerking)
        span.end()
    }

    /**
     * Voert een blok code uit binnen een LDV-span.
     *
     * De span wordt automatisch beëindigd na uitvoering van het blok.
     * Bij een exception wordt de fout geregistreerd op de span.
     *
     * @param T het returntype van het blok
     * @param verwerking de dataverwerking die wordt gelogd
     * @param block het uit te voeren blok
     * @return het resultaat van het blok
     */
    fun <T> withinVerwerking(verwerking: LdvVerwerking, block: () -> T): T {
        val span = spanBuilder.startSpan(verwerking)
        val scope = span.makeCurrent()
        return try {
            block()
        } catch (e: Throwable) {
            span.setStatus(StatusCode.ERROR, e.message ?: "Onbekende fout")
            span.recordException(e)
            throw e
        } finally {
            scope.close()
            span.end()
        }
    }

    companion object {
        /**
         * Maakt een [LdvLogger] aan met de opgegeven [OpenTelemetry] instantie.
         */
        fun create(openTelemetry: OpenTelemetry): LdvLogger {
            return LdvLogger(LdvSpanBuilder.create(openTelemetry))
        }
    }
}
