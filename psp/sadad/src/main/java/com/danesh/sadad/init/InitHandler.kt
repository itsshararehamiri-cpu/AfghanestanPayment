package com.danesh.sadad.init

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitRequest
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadNetworkResult
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.key.SadadKeyMaterial
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitHandler @Inject constructor(
    private val initMessageBuilder: SadadInitMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
    private val configurationStore: DeviceConfigurationStore,
    private val deviceOperations: PspDeviceOperations,
) : HandlerTransaction<InitRequest, SadadNetworkResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: InitRequest): IsoMessage =
        initMessageBuilder.build()

    override fun queueFailure(request: InitRequest): SadadNetworkResult {
        val message = buildMessage(request)
        return SadadNetworkResult(
            detail = transport.map(
                transactionType = TransactionType.INIT,
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
        request: InitRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.map(
            transactionType = TransactionType.INIT,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: InitRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult {
        return try {
            kotlinx.coroutines.runBlocking {
                deviceOperations.completeInit(
                    request = request,
                    terminalKey = SadadKeyMaterial.masterKeyBytes(),
                )
            }
            configurationStore.markConfigured()
            SadadNetworkResult(
                detail = transport.map(
                    transactionType = TransactionType.INIT,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                    masterKey = SadadKeyConfig.MASTER_KEY_HEX,
                ),
            )
        } catch (error: Exception) {
            SadadNetworkResult(
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
        request: InitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: InitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: InitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: InitRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadNetworkResult = receiveFailure(request, sentMessage, e)
}
