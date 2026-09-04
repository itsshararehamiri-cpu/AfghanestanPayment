package com.danesh.api

/**
 * Holds date/time captured when an ISO message is built (field 12),
 * separate from the ISO message payload so it can be used in responses.
 */
interface TransactionSessionClock {
    fun capture(clock: TransactionClock)
    fun current(): TransactionClock?
    fun clear()
}

fun parseTransactionClockFromField12(field12: String): TransactionClock? {
    if (field12.length < 12) return null
    return TransactionClock(
        date = "20${field12.take(6)}",
        time = field12.takeLast(6),
    )
}
