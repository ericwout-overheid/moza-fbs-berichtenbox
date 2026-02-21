package nl.rijksoverheid.moz.notificatieprofiel.exception

import nl.rijksoverheid.moz.common.model.OntvangerIdType

class ProfielNietGevondenException(val ontvangerId: String, val ontvangerIdType: OntvangerIdType) :
    RuntimeException("Profiel voor ontvanger '$ontvangerId' (type $ontvangerIdType) niet gevonden")
