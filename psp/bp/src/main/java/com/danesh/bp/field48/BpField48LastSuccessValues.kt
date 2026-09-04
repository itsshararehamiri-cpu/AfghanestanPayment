package com.danesh.bp.field48

import com.danesh.api.TransactionContextProvider
import com.danesh.bp.key.BpKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpField48LastSuccessValues @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) {
    fun stanTagValue(): String = formatStan(contextProvider.lastSuccessfulStan())

    fun rrnTagValue(): String = formatRrn(contextProvider.lastSuccessfulRrn())

    companion object {
        fun formatStan(raw: String): String =
            raw.filter(Char::isDigit)
                .padStart(BpKeyConfig.LAST_SUCCESS_STAN_LENGTH, '0')
                .takeLast(BpKeyConfig.LAST_SUCCESS_STAN_LENGTH)
                .ifBlank { BpKeyConfig.DEFAULT_LAST_SUCCESS_STAN }

        fun formatRrn(raw: String): String =
            raw.filter(Char::isDigit)
                .padStart(BpKeyConfig.LAST_SUCCESS_RRN_LENGTH, '0')
                .takeLast(BpKeyConfig.LAST_SUCCESS_RRN_LENGTH)
                .ifBlank { BpKeyConfig.DEFAULT_LAST_SUCCESS_RRN }
    }
}
