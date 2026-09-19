package com.danesh.sadad.acceptpin

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadNetworkResult
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcceptPinHandler @Inject constructor(
    private val messageBuilder: SadadAcceptPinMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
) : HandlerTransaction<SadadAcceptPinRequest, SadadNetworkResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: SadadAcceptPinRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: SadadAcceptPinRequest): SadadNetworkResult {
        val message = buildMessage(request)
        return SadadNetworkResult(
            detail = transport.map(
                transactionType = TransactionType.BALANCE,
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
        request: SadadAcceptPinRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: SadadAcceptPinRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: SadadAcceptPinRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadAcceptPinRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadAcceptPinRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadAcceptPinRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadNetworkResult = receiveFailure(request, sentMessage, e)
}
