package com.danesh.hp.logon

import com.danesh.api.DeviceConfigurationStore
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
class LogonHandler @Inject constructor(
    private val logonMessageBuilder: HpLogonMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
    private val deviceOperations: PspDeviceOperations,
    private val deviceWorkflow: HpDeviceWorkflow,
    private val configurationStore: DeviceConfigurationStore,
) : HandlerTransaction<HpLogonRequest, HpLogonResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpLogonRequest): IsoMessage =
        logonMessageBuilder.build()

    override fun queueFailure(request: HpLogonRequest): HpLogonResult {
        val message = buildMessage(request)
        return HpLogonResult(
            detail = transport.map(
                transactionType = TransactionType.LOGON,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
                masterKey = "",
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpLogonRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpLogonResult = HpLogonResult(
        detail = transport.map(
            transactionType = TransactionType.LOGON,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
            masterKey = "",
        ),
    )

    override fun success(
        request: HpLogonRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpLogonResult {
        return try {
            if (response != null) {
                kotlinx.coroutines.runBlocking {
                    val workingKeys = deviceWorkflow.hardcodedWorkingKeys()
                    deviceOperations.completeLogon(workingKeys)
                    configurationStore.markConfigured()
                }
            }
            HpLogonResult(
                detail = transport.map(
                    transactionType = TransactionType.LOGON,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                    masterKey = "",
                ),
            )
        } catch (error: Exception) {
            HpLogonResult(
                detail = transport.failureDetail(
                    transactionType = TransactionType.LOGON,
                    sentMessage = sentMessage,
                    response = response,
                    responseCode = "96",
                    responseMessage = error.message.orEmpty().ifBlank { messages.failed() },
                ),
            )
        }
    }

    override fun connectFailure(
        request: HpLogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpLogonResult = HpLogonResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.LOGON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpLogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpLogonResult = HpLogonResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.LOGON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpLogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpLogonResult = HpLogonResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.LOGON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpLogonRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpLogonResult = receiveFailure(request, sentMessage, e)
}
