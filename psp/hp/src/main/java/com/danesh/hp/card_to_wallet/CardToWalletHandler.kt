package com.danesh.hp.card_to_wallet

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
class CardToWalletHandler @Inject constructor(
    private val messageBuilder: HpCardToWalletMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpCardToWalletRequest, HpCardToWalletResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needAdvice: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: HpCardToWalletRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: HpCardToWalletRequest): HpCardToWalletResult {
        val message = buildMessage(request)
        return HpCardToWalletResult(
            detail = transport.map(
                transactionType = TransactionType.CARD_TO_WALLET,
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
        request: HpCardToWalletRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCardToWalletResult = HpCardToWalletResult(
        detail = transport.map(
            transactionType = TransactionType.CARD_TO_WALLET,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: HpCardToWalletRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCardToWalletResult = HpCardToWalletResult(
        detail = transport.map(
            transactionType = TransactionType.CARD_TO_WALLET,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpCardToWalletRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCardToWalletResult = HpCardToWalletResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_WALLET,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpCardToWalletRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCardToWalletResult = HpCardToWalletResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_WALLET,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpCardToWalletRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCardToWalletResult = HpCardToWalletResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CARD_TO_WALLET,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpCardToWalletRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpCardToWalletResult = receiveFailure(request, sentMessage, e)
}
