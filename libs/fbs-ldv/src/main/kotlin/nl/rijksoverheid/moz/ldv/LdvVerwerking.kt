package nl.rijksoverheid.moz.ldv

import java.net.URI

/**
 * Beschrijving van een dataverwerking voor het Logboek Dataverwerkingen.
 *
 * @property verwerkingsActiviteitId URI van de verwerkingsactiviteit uit het verwerkingsregister
 * @property betrokkeneId identifier van de betrokkene (data subject)
 * @property betrokkeneIdType type van de betrokkene identifier (bijv. "BSN")
 * @property operatieNaam naam van de operatie die wordt uitgevoerd
 */
data class LdvVerwerking(
    val verwerkingsActiviteitId: URI,
    val betrokkeneId: String,
    val betrokkeneIdType: String,
    val operatieNaam: String
)
