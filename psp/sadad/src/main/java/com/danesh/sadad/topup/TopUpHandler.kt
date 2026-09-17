package com.danesh.sadad.topup


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
class TopUpHandler @Inject constructor(

    private val builder: SadadTopUpMessageBuilder,
    private val  messages: SadadTransactionMessages,
    private val  transport: SadadIsoHandlerSupport,
)  : HandlerTransaction<SadadTopUpRequest, SadadTopUpResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: SadadTopUpRequest): IsoMessage
    {
        return kotlinx.coroutines.runBlocking {
            builder.build(request)
        }
    }

    override fun queueFailure(request: SadadTopUpRequest): SadadTopUpResult {
        val message = buildMessage(request)
        return SadadTopUpResult(
            detail = transport.map(
                transactionType = TransactionType.BALANCE,
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
        request: SadadTopUpRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadTopUpResult = SadadTopUpResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: SadadTopUpRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadTopUpResult = SadadTopUpResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: SadadTopUpRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTopUpResult = SadadTopUpResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadTopUpRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTopUpResult = SadadTopUpResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadTopUpRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTopUpResult = SadadTopUpResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadTopUpRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadTopUpResult = receiveFailure(request, sentMessage, e)
}
