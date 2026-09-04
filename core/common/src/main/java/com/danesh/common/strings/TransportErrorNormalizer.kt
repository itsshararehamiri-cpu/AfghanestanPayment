package com.danesh.common.strings

import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes

object TransportErrorNormalizer {

    fun normalize(
        detail: TransactionResultDetail,
        appStrings: AppStrings,
    ): TransactionResultDetail {
        val normalizedCode = TransactionTransportCodes.normalizeCode(detail.responseCode)
        if (!TransactionTransportCodes.isTransportCode(normalizedCode)) {
            return detail
        }
        return detail.copy(
            responseCode = normalizedCode,
            responseMessage = appStrings.transportMessageFor(normalizedCode),
        )
    }
}
