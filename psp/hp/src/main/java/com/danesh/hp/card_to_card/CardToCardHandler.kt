package com.danesh.hp.card_to_card

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardToCardHandler @Inject constructor(
    private val cardToCardMessageBuilder: HpCardToCardMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpCardToCardRequest, HpCardToCardResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needAdvice: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: HpCardToCardRequest): IsoMessage =
        cardToCardMessageBuilder.build(request)

    override fun queueFailure(request: HpCardToCardRequest): HpCardToCardResult {
        val message = buildMessage(request)
        return HpCardToCardResult(
            detail = transport.map(
                transactionType = TransactionType.CARD_TO_CARD,
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
        request: HpCardToCardRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCardToCardResult = HpCardToCardResult(
        detail = transport.map(
            transactionType = TransactionType.CARD_TO_CARD,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: HpCardToCardRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCardToCardResult = HpCardToCardResult(
        detail = transport.map(
            transactionType = TransactionType.CARD_TO_CARD,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpCardToCardRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCardToCardResult = HpCardToCardResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_CARD,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpCardToCardRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCardToCardResult = HpCardToCardResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_CARD,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpCardToCardRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCardToCardResult = HpCardToCardResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_CARD,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpCardToCardRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpCardToCardResult = receiveFailure(request, sentMessage, e)
}
