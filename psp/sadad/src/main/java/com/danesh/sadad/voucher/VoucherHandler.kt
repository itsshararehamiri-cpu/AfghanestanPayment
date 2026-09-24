package com.danesh.sadad.voucher

import android.util.Log
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.api.VoucherUserInput
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadTxnResult
import com.danesh.sadad.util.IranSystemEncoding
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherHandler @Inject constructor(
    private val builder: SadadVoucherMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
    private val pinCipher: SadadChargePinCipher,
) : HandlerTransaction<VoucherUserInput, SadadTxnResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: VoucherUserInput): IsoMessage =
        kotlinx.coroutines.runBlocking { builder.build(request) }

    override fun queueFailure(request: VoucherUserInput): SadadTxnResult {
        val message = buildMessage(request)
        return SadadTxnResult(
            detail = transport.map(
                transactionType = TransactionType.VOUCHER,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: VoucherUserInput,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadTxnResult = SadadTxnResult(
        detail = overlayChargeResponse(
            transport.map(
                transactionType = TransactionType.VOUCHER,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
            response,
        ),
    )

    override fun success(
        request: VoucherUserInput,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadTxnResult = SadadTxnResult(
        detail = overlayChargeResponse(
            transport.map(
                transactionType = TransactionType.VOUCHER,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success(),
            ),
            response,
        ),
    )

    override fun connectFailure(
        request: VoucherUserInput,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.VOUCHER,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: VoucherUserInput,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.VOUCHER,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: VoucherUserInput,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.VOUCHER,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: VoucherUserInput,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadTxnResult = receiveFailure(request, sentMessage, e)

    private fun overlayChargeResponse(
        detail: com.danesh.api.TransactionResultDetail,
        response: IsoMessage?,
    ): com.danesh.api.TransactionResultDetail {
        if (response == null) {
            Log.d(TAG, "1) response=null approved=false")
            return detail
        }
        val approved = IsoResponseCodes.isApproved(response.responseCode)
        Log.d(TAG, "1) response code=${response.responseCode} approved=$approved")
        val field62 = response.privateUseField62
        Log.d(TAG, "2) field62 raw string len=${field62.length} value=$field62")
        val field62Bytes = response.privateUseField62Bytes()
        Log.d(
            TAG,
            "3) field62 bytes len=${field62Bytes?.size ?: 0} hex=${field62Bytes.toHexOrEmpty()}",
        )
        val pins = if (approved) {
            SadadChargeField62Parser.parse(field62)
                ?: SadadChargeField62Parser.parse(field62Bytes)
        } else {
            Log.d(TAG, "4) skip decode; response code is not 00")
            Log.d(TAG, "5) token count=0 serial= pin= ussd=")
            null
        }
        val serial = pins?.serial.orEmpty().ifBlank { detail.voucherSerial }
        val pin = pins?.let(pinCipher::reveal).orEmpty().ifBlank { detail.voucherPin }
        val ussd = pins?.ussd.orEmpty().ifBlank { detail.voucherMethod.orEmpty() }
        Log.d(TAG, "6) written voucherSerial=$serial voucherPin=$pin voucherUssd=$ussd")
        logField64(response)
        val printText = IranSystemEncoding.fieldToUtf8(
            response.additionalResponseData.toByteArray(Charsets.ISO_8859_1),
        )
        return detail.copy(
            voucherSerial = serial,
            voucherPin = pin,
            voucherMethod = ussd.ifBlank { detail.voucherMethod },
            responseMessage = printText.ifBlank { detail.responseMessage },
        )
    }

    private fun logField64(response: IsoMessage) {
        val iso = runCatching { response.getIsoMessage() }.getOrNull()
        val raw = iso?.getString(64).orEmpty()
        val bytes = iso?.getBytes(64) ?: response.mac
        Log.d(
            TAG,
            "8) field64 present=${!raw.isEmpty() || bytes != null} " +
                "raw=$raw hex=${bytes.toHexOrEmpty()} length=${bytes?.size ?: raw.length}",
        )
    }

    private fun ByteArray?.toHexOrEmpty(): String {
        if (this == null || isEmpty()) return ""
        return ISOUtil.hexString(this)
    }

    private companion object {
        const val TAG = "sharjHoma"
    }
}
