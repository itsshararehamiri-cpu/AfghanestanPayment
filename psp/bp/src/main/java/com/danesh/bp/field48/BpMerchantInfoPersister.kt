package com.danesh.bp.field48

import com.danesh.api.TransactionContextProvider
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpMerchantInfoPersister @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) {
    fun persistFromResponse(response: IsoMessage) {
        response.unpackField48()
        val current = contextProvider.getTerminalConfig()

        val rawMerchantNameFa = tagValue(response, BpField48Tags.MERCHANT_NAME_FA)
        val rawMerchantNameEn = tagValue(response, BpField48Tags.MERCHANT_NAME_EN)
        val decodedMerchantNameFa = rawMerchantNameFa?.let(BpCp1256TextDecoder::decode)
        val decodedMerchantNameEn = rawMerchantNameEn?.let(BpCp1256TextDecoder::decode)

        logMerchantNameDecoding(
            rawFa = rawMerchantNameFa,
            decodedFa = decodedMerchantNameFa,
            rawEn = rawMerchantNameEn,
            decodedEn = decodedMerchantNameEn,
        )

        val updated = current.copy(
            merchantName = decodedMerchantNameFa ?: current.merchantName,
            merchantPhone = tagValue(response, BpField48Tags.MERCHANT_PHONE) ?: current.merchantPhone,
            merchantAddress = tagValue(response, BpField48Tags.MERCHANT_ADDRESS) ?: current.merchantAddress,
            englishMerchantName = decodedMerchantNameEn ?: current.englishMerchantName,
            merchantPostalCode = tagValue(response, BpField48Tags.MERCHANT_POSTAL_CODE)
                ?: current.merchantPostalCode,
        )
        if (updated != current) {
            contextProvider.saveTerminalConfig(updated)
        }
    }

    private fun logMerchantNameDecoding(
        rawFa: String?,
        decodedFa: String?,
        rawEn: String?,
        decodedEn: String?,
    ) {
        if (rawFa != null || rawEn != null) {
        }
    }

    private fun tagValue(response: IsoMessage, tag: String): String? =
        response.getField48Tag(tag)?.trim()?.takeIf { it.isNotEmpty() }

    private companion object {
        private const val TAG = "BpMerchantInfoPersister"
    }
}
