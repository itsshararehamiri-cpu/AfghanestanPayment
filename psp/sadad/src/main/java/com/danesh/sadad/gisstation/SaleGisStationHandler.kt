package com.danesh.sadad.gisstation

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
class SaleGisStationHandler @Inject constructor(
    private val messageBuilder: SadadSaleGisStationMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
) : HandlerTransaction<SadadSaleGisStationRequest, SadadNetworkResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: SadadSaleGisStationRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: SadadSaleGisStationRequest): SadadNetworkResult {
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
        request: SadadSaleGisStationRequest,
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
        request: SadadSaleGisStationRequest,
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
        request: SadadSaleGisStationRequest,
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
        request: SadadSaleGisStationRequest,
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
        request: SadadSaleGisStationRequest,
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
        request: SadadSaleGisStationRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadNetworkResult = receiveFailure(request, sentMessage, e)
}
