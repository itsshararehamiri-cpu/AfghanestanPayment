package com.danesh.sadad.gambond

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
class GamBondHandler @Inject constructor(
    private val messageBuilder: SadadGamBondMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
) : HandlerTransaction<SadadGamBondRequest, SadadNetworkResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: SadadGamBondRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: SadadGamBondRequest): SadadNetworkResult {
        val message = buildMessage(request)
        return SadadNetworkResult(
            detail = transport.map(
                transactionType = TransactionType.PURCHASE,
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
        request: SadadGamBondRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: SadadGamBondRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: SadadGamBondRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadGamBondRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadGamBondRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadGamBondRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadNetworkResult = receiveFailure(request, sentMessage, e)
}
