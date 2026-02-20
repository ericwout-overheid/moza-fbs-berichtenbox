package nl.fbs.common.model

import java.time.Instant

/**
 * Digitale bereikbaarheidsstatus van een organisatie.
 *
 * @property digitaalBereikbaar of de organisatie digitaal bereikbaar is
 * @property registratieDatum datum van registratie
 * @property intrekkingsDatum datum van intrekking (null indien actief)
 */
data class Bereikbaarheid(
    val digitaalBereikbaar: Boolean,
    val registratieDatum: Instant,
    val intrekkingsDatum: Instant? = null
)
