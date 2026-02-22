package nl.rijksoverheid.moz.digitalebereikbaarheid.exception

import nl.rijksoverheid.moz.common.model.OntvangerIdType

class BereikbaarheidNietGevondenException(val ontvangerId: String, val ontvangerIdType: OntvangerIdType) :
    RuntimeException("Bereikbaarheid voor ontvanger '$ontvangerId' (type $ontvangerIdType) niet gevonden")
