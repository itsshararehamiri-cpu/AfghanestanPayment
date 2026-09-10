package com.danesh.hp.signon

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.key.HpKeyConfig
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignOnHandler @Inject constructor(
    private val signOnMessageBuilder: HpSignOnMessageHandler,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
    private val configurationStore: DeviceConfigurationStore,
    private val deviceOperations: PspDeviceOperations,
) : HandlerTransaction<HpSignOnRequest, HpSignOnResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpSignOnRequest): IsoMessage =
        signOnMessageBuilder.build()
    override fun queueFailure(request: HpSignOnRequest): HpSignOnResult {
        val message = buildMessage(request)
        return HpSignOnResult(
            detail = transport.map(
                transactionType = TransactionType.SIGNON,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
                masterKey = "",
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =(response?.responseCode!="800")
        //IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpSignOnRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpSignOnResult = HpSignOnResult(
        detail = transport.map(
            transactionType = TransactionType.SIGNON,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
            masterKey = "",
        ),
    )

    override fun success(
        request: HpSignOnRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpSignOnResult {
        return try {
            configurationStore.markConfigured()
            HpSignOnResult(
                detail = transport.map(
                    transactionType = TransactionType.INIT,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                    masterKey = HpKeyConfig.MASTER_KEY_HEX,
                ),
            )
        } catch (error: Exception) {
            HpSignOnResult(
                detail = transport.failureDetail(
                    transactionType = TransactionType.INIT,
                    sentMessage = sentMessage,
                    response = response,
                    responseCode = "96",
                    responseMessage = error.message.orEmpty().ifBlank { messages.failed() },
                ),
            )
        }
    }

    override fun connectFailure(
        request: HpSignOnRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpSignOnResult = HpSignOnResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpSignOnRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpSignOnResult = HpSignOnResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.SIGNON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpSignOnRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpSignOnResult = HpSignOnResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpSignOnRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpSignOnResult = receiveFailure(request, sentMessage, e)
}
