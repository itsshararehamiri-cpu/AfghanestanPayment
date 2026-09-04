package com.danesh.engine

import com.danesh.api.TransactionClock
import com.danesh.api.TransactionSessionClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTransactionSessionClock @Inject constructor() : TransactionSessionClock {

    @Volatile
    private var clock: TransactionClock? = null

    override fun capture(clock: TransactionClock) {
        this.clock = clock
    }

    override fun current(): TransactionClock? = clock

    override fun clear() {
        clock = null
    }
}
