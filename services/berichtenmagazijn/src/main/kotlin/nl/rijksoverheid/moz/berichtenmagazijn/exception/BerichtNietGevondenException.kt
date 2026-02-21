package nl.rijksoverheid.moz.berichtenmagazijn.exception

import java.util.UUID

class BerichtNietGevondenException(val berichtId: UUID) :
    RuntimeException("Bericht met id '$berichtId' niet gevonden")
