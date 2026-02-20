package nl.fbs.common.model

/**
 * Notificatieprofiel van een ontvanger.
 *
 * @property emailNotificaties of e-mailnotificaties zijn ingeschakeld
 * @property smsNotificaties of SMS-notificaties zijn ingeschakeld
 * @property emailAdres e-mailadres voor notificaties (verplicht als emailNotificaties is true)
 * @property frequentie gewenste notificatiefrequentie
 */
data class Profiel(
    val emailNotificaties: Boolean,
    val smsNotificaties: Boolean,
    val emailAdres: String? = null,
    val frequentie: NotificatieFrequentie = NotificatieFrequentie.DIRECT
)
