package com.danesh.hp.wallet_to_wallet

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
class WalletToWalletHandler @Inject constructor(
    private val messageBuilder: HpWalletToWalletMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpWalletToWalletRequest, HpWalletToWalletResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needAdvice: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: HpWalletToWalletRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: HpWalletToWalletRequest): HpWalletToWalletResult {
        val message = buildMessage(request)
        return HpWalletToWalletResult(
            detail = transport.map(
                transactionType = TransactionType.WALLET_TO_WALLET,
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
        request: HpWalletToWalletRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpWalletToWalletResult = HpWalletToWalletResult(
        detail = transport.map(
            transactionType = TransactionType.WALLET_TO_WALLET,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: HpWalletToWalletRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpWalletToWalletResult = HpWalletToWalletResult(
        detail = transport.map(
            transactionType = TransactionType.WALLET_TO_WALLET,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpWalletToWalletRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpWalletToWalletResult = HpWalletToWalletResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.WALLET_TO_WALLET,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpWalletToWalletRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpWalletToWalletResult = HpWalletToWalletResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.WALLET_TO_WALLET,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpWalletToWalletRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpWalletToWalletResult = HpWalletToWalletResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.WALLET_TO_WALLET,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpWalletToWalletRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpWalletToWalletResult = receiveFailure(request, sentMessage, e)
}
