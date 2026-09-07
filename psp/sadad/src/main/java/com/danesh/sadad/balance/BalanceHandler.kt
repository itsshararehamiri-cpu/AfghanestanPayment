package com.danesh.sadad.balance

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
class BalanceHandler @Inject constructor(
  private val balanceMessageBuilder: SadadBalanceMessageBuilder,
   private val messages: SadadTransactionMessages,
   private val transport: SadadIsoHandlerSupport,
)  : HandlerTransaction<SadadBalanceRequest, SadadBalanceResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: SadadBalanceRequest): IsoMessage
    {
        return kotlinx.coroutines.runBlocking {
            balanceMessageBuilder.build(request)
        }
    }

    override fun queueFailure(request: SadadBalanceRequest): SadadBalanceResult {
        val message = buildMessage(request)
        return SadadBalanceResult(
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
        request: SadadBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBalanceResult = SadadBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: SadadBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBalanceResult = SadadBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: SadadBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBalanceResult = SadadBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBalanceResult = SadadBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBalanceResult = SadadBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadBalanceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadBalanceResult = receiveFailure(request, sentMessage, e)
}
