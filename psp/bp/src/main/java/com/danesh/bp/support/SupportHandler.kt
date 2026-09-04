package com.danesh.bp.support

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportHandler @Inject constructor(
    private val supportMessageBuilder: BpSupportMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpSupportRequest, BpSupportResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true

    override fun buildMessage(request: BpSupportRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            supportMessageBuilder.build(request)
        }

    override fun queueFailure(request: BpSupportRequest): BpSupportResult {
        val message = buildMessage(request)
        return BpSupportResult(
            detail = transport.map(
                transactionType = TransactionType.SUPPORT,
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
        request: BpSupportRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpSupportResult = BpSupportResult(
        detail = transport.map(
            transactionType = TransactionType.SUPPORT,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: BpSupportRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpSupportResult = BpSupportResult(
        detail = transport.map(
            transactionType = TransactionType.SUPPORT,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: BpSupportRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpSupportResult = BpSupportResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.SUPPORT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpSupportRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpSupportResult = BpSupportResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.SUPPORT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpSupportRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpSupportResult = BpSupportResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.SUPPORT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpSupportRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpSupportResult = receiveFailure(request, sentMessage, e)
}
