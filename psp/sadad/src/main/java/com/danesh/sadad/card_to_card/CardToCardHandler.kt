package com.danesh.sadad.card_to_card


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
class CardToCardHandler @Inject constructor(
    private val cardToCardMessageBuilder: SadadCardToCardMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
)  : HandlerTransaction<SadadCardToCardRequest, SadadCardToCardResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: SadadCardToCardRequest): IsoMessage
    {
        return kotlinx.coroutines.runBlocking {
            cardToCardMessageBuilder.build(request)
        }
    }

    override fun queueFailure(request: SadadCardToCardRequest): SadadCardToCardResult {
        val message = buildMessage(request)
        return SadadCardToCardResult(
            detail = transport.map(
                transactionType = TransactionType.CARD_TO_CARD,
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
        request: SadadCardToCardRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadCardToCardResult = SadadCardToCardResult(
        detail = transport.map(
            transactionType = TransactionType.CARD_TO_CARD,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: SadadCardToCardRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadCardToCardResult = SadadCardToCardResult(
        detail = transport.map(
            transactionType = TransactionType.CARD_TO_CARD,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: SadadCardToCardRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadCardToCardResult = SadadCardToCardResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_CARD,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadCardToCardRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadCardToCardResult = SadadCardToCardResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_CARD,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadCardToCardRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadCardToCardResult = SadadCardToCardResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_CARD,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadCardToCardRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadCardToCardResult = receiveFailure(request, sentMessage, e)
}
