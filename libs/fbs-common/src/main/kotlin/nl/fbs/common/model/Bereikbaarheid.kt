package nl.fbs.common.model

import java.time.Instant

/**
 * Digitale bereikbaarheidsstatus van een ontvanger.
 *
 * @property ontvangerId identificatie van de ontvanger
 * @property ontvangerIdType type identificatie van de ontvanger
 * @property digitaalBereikbaar of de ontvanger toestemming heeft gegeven voor digitale berichtgeving
 * @property registratieDatum datum waarop de toestemming is geregistreerd
 * @property intrekkingsDatum datum waarop de toestemming is ingetrokken (null indien niet ingetrokken)
 */
data class Bereikbaarheid(
    val ontvangerId: String,
    val ontvangerIdType: OntvangerIdType,
    val digitaalBereikbaar: Boolean,
    val registratieDatum: Instant,
    val intrekkingsDatum: Instant? = null
)
