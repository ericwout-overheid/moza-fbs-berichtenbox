package nl.fbs.common.model

/**
 * Frequentie waarmee notificaties worden gebundeld en verzonden.
 */
enum class NotificatieFrequentie {
    /** Notificatie wordt direct verzonden */
    DIRECT,

    /** Notificaties worden dagelijks gebundeld */
    DAGELIJKS,

    /** Notificaties worden wekelijks gebundeld */
    WEKELIJKS
}
