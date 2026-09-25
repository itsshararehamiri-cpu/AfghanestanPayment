package com.danesh.sadad.util

import android.util.Log
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.common.strings.TransportErrorNormalizer
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoTransactionResultMapper
import com.danesh.sadad.field54.SadadField54Parser
import com.danesh.sadad.init.SadadInitProfileStore
import com.danesh.sadad.iso.SadadIsoMessageFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadIsoHandlerSupport @Inject constructor(
    private val isoMessageFactory: SadadIsoMessageFactory,
    private val messages: SadadTransactionMessages,
    private val resultMapper: IsoTransactionResultMapper,
    private val responseCodeText: SadadResponseCodeText,
    private val contextProvider: TransactionContextProvider,
    private val initProfileStore: SadadInitProfileStore,
) {
    fun map(
        transactionType: TransactionType,
        request: IsoMessage,
        response: IsoMessage?,
        isSuccess: Boolean,
        responseMessage: String,
        masterKey: String = "",
    ): TransactionResultDetail {
        val parsed = resultMapper.map(
            transactionType = transactionType,
            request = request,
            response = response,
            isSuccess = isSuccess,
            responseMessage = responseMessage,
            merchantId = isoMessageFactory.terminalMerchantId(),
            merchantName = isoMessageFactory.terminalMerchantName(),
            merchantPhone = isoMessageFactory.merchantPhone(),
            masterKey = masterKey,
        )
        val balances = response?.additionalAmounts
            ?.takeIf { it.isNotBlank() }
            ?.let { SadadField54Parser.parse(it) }
        val normalized = TransportErrorNormalizer.normalize(
            parsed.copy(
                actualBalance = balances?.actual,
                availableBalance = balances?.available,
            ),
            messages.appStrings,
        )
        val withMessage = normalized.copy(responseMessage = mappedResponseMessage(normalized))
        val hostData = SadadHostFunctionCodes.parse(response?.privateUseField63)
        if (isSuccess) {
            saveTopupVat(hostData)
            saveTerminalUniqueCode(hostData, withMessage.terminalId)
        }
        val terminalUniqueCode = runCatching { initProfileStore.get().taxMemoryUniqueCode }
            .getOrDefault("")
        Log.d(
            SadadHostFunctionCodes.TUC_TAG,
            "map type=$transactionType success=$isSuccess terminalId='${withMessage.terminalId}' " +
                "codesFrom043=${hostData.terminalUniqueCodes} storedCode='$terminalUniqueCode' " +
                "-> receipt terminalUniqueCode='$terminalUniqueCode'",
        )
        return SadadHostDataReceipt.apply(withMessage, hostData)
            .copy(terminalUniqueCode = terminalUniqueCode)
    }

    /** Function Code 043: کد کارتخوان این پایانه (همان Unique Code فانکشن‌کد 013). */
    private fun saveTerminalUniqueCode(hostData: SadadHostData, terminalId: String) {
        val codes = hostData.terminalUniqueCodes
        if (codes.isEmpty()) return
        val code = codes[terminalId.trim()] ?: codes.values.singleOrNull() ?: run {
            Log.w(
                SadadHostFunctionCodes.TUC_TAG,
                "043 has no code for terminalId='${terminalId.trim()}' codes=$codes -> not saved",
            )
            return
        }
        runCatching {
            val profile = initProfileStore.get()
            if (profile.taxMemoryUniqueCode != code) {
                initProfileStore.save(profile.copy(taxMemoryUniqueCode = code))
                Log.d(SadadHostFunctionCodes.TUC_TAG, "043 saved code='$code' (old='${profile.taxMemoryUniqueCode}')")
            } else {
                Log.d(SadadHostFunctionCodes.TUC_TAG, "043 code='$code' unchanged")
            }
        }.onFailure { Log.e(SadadHostFunctionCodes.TUC_TAG, "043 save failed", it) }
    }

    /** Function Code 018: درصد مالیات شارژ اعلام‌شده توسط سوئیچ. */
    private fun saveTopupVat(hostData: SadadHostData) {
        val percent = hostData.topupVatPercent ?: return
        val rounded = Math.round(percent).toInt()
        if (rounded !in 1..100) return
        runCatching { contextProvider.saveVatPercentage(rounded.toString()) }
    }

    private fun mappedResponseMessage(detail: TransactionResultDetail): String {
        if (TransactionTransportCodes.isTransportCode(detail.responseCode)) {
            return detail.responseMessage
        }
        return responseCodeText.describe(detail.responseCode) ?: detail.responseMessage
    }

    fun failureDetail(
        transactionType: TransactionType,
        sentMessage: IsoMessage,
        response: IsoMessage?,
        responseCode: String,
        responseMessage: String,
    ): TransactionResultDetail =
        TransportErrorNormalizer.normalize(
            map(
                transactionType = transactionType,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = responseMessage,
            ).copy(responseCode = TransactionTransportCodes.normalizeCode(responseCode)),
            messages.appStrings,
        )

    fun connectFailedMessage(error: Exception): String = messages.connectFailed()

    fun sendFailedMessage(error: Exception): String = messages.sendFailed()

    fun receiveFailedMessage(error: Exception): String = messages.receiveFailed()
}
