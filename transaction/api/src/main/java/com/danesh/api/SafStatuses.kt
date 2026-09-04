package com.danesh.api


object SafStatuses {
    const val NEEDS_REVERSE = 'R'

    const val NEEDS_ADVICE = 'S'

    fun needsAdvice(status: Char): Boolean = status == NEEDS_ADVICE
    fun needsReverse(status: Char): Boolean = status == NEEDS_REVERSE
}
