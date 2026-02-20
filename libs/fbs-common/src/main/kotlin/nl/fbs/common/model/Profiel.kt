package nl.fbs.common.model

/**
 * Notificatieprofiel van een ontvanger.
 *
 * @property ontvangerId identificatie van de ontvanger
 * @property ontvangerIdType type identificatie van de ontvanger
 * @property emailNotificaties of e-mailnotificaties zijn ingeschakeld
 * @property smsNotificaties of SMS-notificaties zijn ingeschakeld
 * @property emailAdres e-mailadres voor notificaties (verplicht als emailNotificaties is true)
 * @property telefoonnummer telefoonnummer voor SMS-notificaties (verplicht als smsNotificaties is true)
 * @property frequentie gewenste notificatiefrequentie
 */
data class Profiel(
    val ontvangerId: String,
    val ontvangerIdType: OntvangerIdType,
    val emailNotificaties: Boolean,
    val smsNotificaties: Boolean,
    val emailAdres: String? = null,
    val telefoonnummer: String? = null,
    val frequentie: NotificatieFrequentie = NotificatieFrequentie.DIRECT
) {
    init {
        require(!emailNotificaties || !emailAdres.isNullOrBlank()) {
            "emailAdres is verplicht wanneer emailNotificaties is ingeschakeld"
        }
        require(!smsNotificaties || !telefoonnummer.isNullOrBlank()) {
            "telefoonnummer is verplicht wanneer smsNotificaties is ingeschakeld"
        }
    }
}
