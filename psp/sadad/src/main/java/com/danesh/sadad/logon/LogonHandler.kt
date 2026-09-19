package com.danesh.sadad.logon

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.IsoResponseCodes
import com.danesh.api.LogonRequest
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadNetworkResult
import com.danesh.sadad.device.SadadDeviceWorkflow
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogonHandler @Inject constructor(
    private val logonMessageBuilder: SadadLogonMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
    private val deviceOperations: PspDeviceOperations,
    private val deviceWorkflow: SadadDeviceWorkflow,
    private val configurationStore: DeviceConfigurationStore,
    private val contextProvider: TransactionContextProvider,
) : HandlerTransaction<LogonRequest, SadadNetworkResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: LogonRequest): IsoMessage =
        logonMessageBuilder.build()

    override fun queueFailure(request: LogonRequest): SadadNetworkResult {
        val message = buildMessage(request)
        return SadadNetworkResult(
            detail = transport.map(
                transactionType = TransactionType.LOGON,
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
        request: LogonRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.map(
            transactionType = TransactionType.LOGON,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: LogonRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadNetworkResult {
        response?.getDump()
        return try {// TODO:
            if (response != null) {
                persistTerminalIds(response)
                kotlinx.coroutines.runBlocking {
//                    deviceOperations.completeLogon(deviceWorkflow.hardcodedWorkingKeys())
//                    configurationStore.markConfigured()
                }
            }
            SadadNetworkResult(
                detail = transport.map(
                    transactionType = TransactionType.LOGON,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                ),
            )
        } catch (error: Exception) {
            SadadNetworkResult(
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
        request: LogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.LOGON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: LogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.LOGON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: LogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadNetworkResult = SadadNetworkResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.LOGON,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: LogonRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadNetworkResult = receiveFailure(request, sentMessage, e)

    /**
     * DE41/DE42 پاسخ LOGON — سوئیچ شماره ترمینال/پذیرنده را برمی‌گرداند و باید
     * ذخیره شود، وگرنه تراکنش‌های بعدی این فیلدها را خالی می‌فرستند.
     */
    private fun persistTerminalIds(response: IsoMessage) {
        val current = contextProvider.getTerminalConfig()
        val terminalId = response.terminalId.takeIf { it.isNotBlank() } ?: current.terminalId
        val merchantId = response.merchantId.takeIf { it.isNotBlank() } ?: current.merchantId
        if (terminalId != current.terminalId || merchantId != current.merchantId) {
            contextProvider.saveTerminalConfig(
                current.copy(terminalId = terminalId, merchantId = merchantId),
            )
        }
    }
}
