package com.danesh.sadad.util

import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.common.strings.TransportErrorNormalizer
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoTransactionResultMapper
import com.danesh.sadad.field54.SadadField54Parser
import com.danesh.sadad.iso.SadadIsoMessageFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadIsoHandlerSupport @Inject constructor(
    private val isoMessageFactory: SadadIsoMessageFactory,
    private val messages: SadadTransactionMessages,
    private val resultMapper: IsoTransactionResultMapper,
    private val responseCodeText: SadadResponseCodeText,
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
        return normalized.copy(responseMessage = mappedResponseMessage(normalized))
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
