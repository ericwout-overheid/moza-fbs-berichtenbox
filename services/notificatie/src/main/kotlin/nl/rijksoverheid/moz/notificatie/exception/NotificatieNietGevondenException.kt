package nl.rijksoverheid.moz.notificatie.exception

import java.util.UUID

class NotificatieNietGevondenException(val notificatieId: UUID) :
    RuntimeException("Notificatie met id '$notificatieId' niet gevonden")
