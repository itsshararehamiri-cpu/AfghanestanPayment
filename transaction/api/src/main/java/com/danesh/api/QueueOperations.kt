package com.danesh.api

/**
 * نوع عملیات store-and-forward در صف.
 * تصمیم اصلی Advice/Reverse بر اساس [SafStatuses] است؛ این فیلد برای سازگاری نگه داشته می‌شود.
 */
object QueueOperations {
    const val ADVICE = 'A'
    const val REVERSE = 'R'

    fun fromSafStatus(status: Char): Char =
        if (SafStatuses.needsAdvice(status)) ADVICE else REVERSE
}
