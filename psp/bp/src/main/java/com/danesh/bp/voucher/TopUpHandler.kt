package com.danesh.bp.voucher

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopUpHandler @Inject constructor(
    private val topUpMessageBuilder: BpTopUpMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpTopUpRequest, BpTopUpResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = true
    override val needAdvice: Boolean = false

    override fun buildMessage(request: BpTopUpRequest): IsoMessage =
        runBlocking {
            topUpMessageBuilder.build(request)
        }

    override fun queueFailure(request: BpTopUpRequest): BpTopUpResult {
        val message = buildMessage(request)
        return BpTopUpResult(
            detail = transport.map(
                transactionType = TransactionType.TOPUP,
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
        request: BpTopUpRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpTopUpResult = BpTopUpResult(
        detail = transport.map(
            transactionType = TransactionType.TOPUP,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: BpTopUpRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpTopUpResult = BpTopUpResult(
        detail = transport.map(
            transactionType = TransactionType.TOPUP,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(), mobileNumber = request.mobileNumber
        ),
    )

    override fun connectFailure(
        request: BpTopUpRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpTopUpResult = BpTopUpResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.TOPUP,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpTopUpRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpTopUpResult = BpTopUpResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.TOPUP,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpTopUpRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpTopUpResult = BpTopUpResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.TOPUP,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpTopUpRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpTopUpResult = receiveFailure(request, sentMessage, e)
}
