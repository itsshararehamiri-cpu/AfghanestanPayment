package com.danesh.common.connection

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDepositIdProvider @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
) {
    fun activeDepositId(): String? {
        if (!connectionPreferences.isDefaultDepositIdEnabled()) {
            return null
        }
        val value = connectionPreferences.getNii().trim()
        if (ConnectionAddressValidator.defaultDepositIdError(value) != null) {
            return null
        }
        return value
    }
}
