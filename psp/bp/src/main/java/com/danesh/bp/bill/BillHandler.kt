package com.danesh.bp.bill


import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.balance.BpBalanceMessageBuilder
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillHandler @Inject constructor(
    private val billMessageBuilder: BpBillMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpBillRequest, BpBillResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: BpBillRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            billMessageBuilder.build(request)
        }

    override fun queueFailure(request: BpBillRequest): BpBillResult {
        val message = buildMessage(request)
        return BpBillResult(
            detail = transport.map(
                transactionType = TransactionType.BILL,
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
        request: BpBillRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpBillResult = BpBillResult(
        detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: BpBillRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpBillResult = BpBillResult(
        detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: BpBillRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpBillResult = BpBillResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpBillRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpBillResult = BpBillResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpBillRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpBillResult = BpBillResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpBillRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpBillResult = receiveFailure(request, sentMessage, e)
}
