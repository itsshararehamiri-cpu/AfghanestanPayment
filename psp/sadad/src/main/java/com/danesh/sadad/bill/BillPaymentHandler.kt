package com.danesh.sadad.bill

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillPaymentHandler @Inject constructor(
    private val billPaymentMessageBuilder: SadadBillPaymentMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val  transport: SadadIsoHandlerSupport,
) : HandlerTransaction<SadadBillPaymentRequest, SadadBillPaymentResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: SadadBillPaymentRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            billPaymentMessageBuilder.build(request)
        }

    override fun queueFailure(request: SadadBillPaymentRequest): SadadBillPaymentResult {
        val message = buildMessage(request)
        return SadadBillPaymentResult(
            detail = transport.map(
                transactionType = TransactionType.PURCHASE,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed()
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillPaymentResult = SadadBillPaymentResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()

        ),
    )

    override fun success(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillPaymentResult = SadadBillPaymentResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillPaymentResult = SadadBillPaymentResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillPaymentResult = SadadBillPaymentResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillPaymentResult = SadadBillPaymentResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadBillPaymentResult = receiveFailure(request, sentMessage, e)
}

