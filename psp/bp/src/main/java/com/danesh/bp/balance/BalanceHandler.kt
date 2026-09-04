package com.danesh.bp.balance

import android.content.Context
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceHandler @Inject constructor(
    private val balanceMessageBuilder: BpBalanceMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,    @ApplicationContext val context: Context,

    ) : HandlerTransaction<BpBalanceRequest, BpBalanceResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: BpBalanceRequest): IsoMessage
        {
            return kotlinx.coroutines.runBlocking {
                balanceMessageBuilder.build(request)
            }
        }

    override fun queueFailure(request: BpBalanceRequest): BpBalanceResult {
        val message = buildMessage(request)
        return BpBalanceResult(
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
        request: BpBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpBalanceResult = BpBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: BpBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpBalanceResult = BpBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: BpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpBalanceResult = BpBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpBalanceResult = BpBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpBalanceResult = BpBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpBalanceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpBalanceResult = receiveFailure(request, sentMessage, e)
}
