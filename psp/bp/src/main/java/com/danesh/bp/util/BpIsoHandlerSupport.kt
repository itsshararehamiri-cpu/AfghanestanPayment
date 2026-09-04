package com.danesh.bp.util

import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.common.strings.TransportErrorNormalizer
import com.danesh.iso.IsoMessage
import com.danesh.bp.iso.BpIsoMessageFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpIsoHandlerSupport @Inject constructor(
    private val isoMessageFactory: BpIsoMessageFactory,
    private val messages: BpTransactionMessages,
    private val resultMapper: BpTransactionResultMapper,
) {
    fun map(
        transactionType: TransactionType,
        request: IsoMessage,
        response: IsoMessage?,
        isSuccess: Boolean,
        responseMessage: String,masterKey: String="",mobileNumber: String=""
    ): TransactionResultDetail
    {
        return TransportErrorNormalizer.normalize(
            resultMapper.map(
                transactionType = transactionType,
                request = request,
                response = response,
                isSuccess = isSuccess,
                responseMessage = responseMessage,
                merchantId = isoMessageFactory.terminalMerchantId(),
                merchantName = isoMessageFactory.terminalMerchantName(),
                merchantPhone = isoMessageFactory.merchantPhone(),
                englishMerchantName = isoMessageFactory.englishMerchantName(),
                merchantAddress = isoMessageFactory.merchantAddress(),
                merchantPostalCode = isoMessageFactory.merchantPostalCode(),
                masterKey=masterKey, mobileNumber = mobileNumber
            ),
            messages.appStrings,
        )
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
