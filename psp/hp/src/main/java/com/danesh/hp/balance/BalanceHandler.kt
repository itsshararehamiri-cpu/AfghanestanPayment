package com.danesh.hp.balance

import android.util.Log
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.device.HpDeviceWorkflow
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceHandler @Inject constructor(

                                             private val balanceMessageBuilder: HpBalanceMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpBalanceRequest, HpBalanceResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: HpBalanceRequest): IsoMessage {

       return balanceMessageBuilder.build(request)
    }
    override fun queueFailure(request: HpBalanceRequest): HpBalanceResult {
        val message = buildMessage(request)
        return HpBalanceResult(
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
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBalanceResult {
        val v=HpBalanceResult(
            detail = transport.map(
                transactionType = TransactionType.BALANCE,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
        )
        Log.d("TAG", "failure: ddddddd$v")

       return v
    }

    override fun success(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBalanceResult = HpBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBalanceResult {
        val c=HpBalanceResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.BALANCE,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.CONNECT_FAILED,
                responseMessage = transport.connectFailedMessage(error),
            ),
        )
        Log.d("TAG", "connectFailure: ddddddd$c")

        return c
    }

    override fun sendFailure(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBalanceResult {
        val b=HpBalanceResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.BALANCE,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.SEND_FAILED,
                responseMessage = transport.sendFailedMessage(error),
            ),
        )
        Log.d("TAG", "sendFailure: ddddddd$b")
        return b
    }

    override fun receiveFailure(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBalanceResult {

        val a= HpBalanceResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.BALANCE,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.RECEIVE_FAILED,
                responseMessage = transport.receiveFailedMessage(error),
            ),
        )
        Log.d("TAG", "receiveFailure: ddddddd$a")
        return a
    }

    override fun networkError(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpBalanceResult = receiveFailure(request, sentMessage, e)
}
