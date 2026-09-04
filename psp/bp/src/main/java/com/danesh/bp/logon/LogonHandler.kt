package com.danesh.bp.logon

import android.util.Log
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.init.wipe
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.core.Device
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogonHandler @Inject constructor(
    private val logonMessageBuilder: BpLogonMessageBuilder,
    private val responseProcessor: BpLogonResponseProcessor,
    private val deviceOperations: PspDeviceOperations,
    private val device: Device,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
    private val configurationStore: DeviceConfigurationStore,
) : HandlerTransaction<BpLogonRequest, BpLogonResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true
    override val recordsLastSuccessReference: Boolean = false

    override fun buildMessage(request: BpLogonRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            device.getCheckValue("readgg")

            logonMessageBuilder.build()
        }

    override fun queueFailure(request: BpLogonRequest): BpLogonResult {
        val message = buildMessage(request)
        return BpLogonResult(
            detail = transport.map(
                transactionType = TransactionType.LOGON,
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
        request: BpLogonRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpLogonResult {
        return BpLogonResult(
            detail = transport.map(
                transactionType = TransactionType.LOGON,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed()
            ),
        )
    }

    override fun success(
        request: BpLogonRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpLogonResult {
        return try {
            if (response != null) {
                kotlinx.coroutines.runBlocking {
                    response.print(">>>")
                    BpLogonTrace.step("LogonHandler", "پردازش پاسخ و استخراج کلیدهای کاری F62")
                    device.getCheckValue("clinc")

                    val workingKeys = responseProcessor.processSuccess(sentMessage, response)
                    try {
                        BpLogonTrace.step(
                            "LogonHandler",
                            "تزریق F62: MAC→loadTmkEncryptedMak, PIN→loadTmkEncryptedPik, DATA→loadTmkEncryptedDek " +
                                    "(tmkIndex=${device.INDEX_TMK} = Terminal از Init؛ نه bootstrap). " +
                                    "اگر direct ciphertext=false → fallback decrypt+plaintext inject طبیعی است.",
                        )
                        deviceOperations.completeLogon(workingKeys)
                        configurationStore.markConfigured()
                        device.getCheckValue("dddddddddddddddgh")

                        BpLogonTrace.step(
                            "LogonHandler",
                            "inject کلیدهای کاری نتیجه=موفق — Logon پروتکل کامل " +
                                "(MAC نرم‌افزار + RC=00 + F62 MAK/PIK/DEK)",
                        )
                    } finally {
                        workingKeys.encryptedMacKey.wipe()
                        workingKeys.encryptedPinKey.wipe()
                        workingKeys.encryptedDataKey.wipe()
                    }
                }
            }
            device.getCheckValue("filmnnnd" +
                    "")

            BpLogonResult(
                detail = transport.map(
                    transactionType = TransactionType.LOGON,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success()
                ),
            )
        } catch (error: Exception) {
            BpLogonTrace.error(
                "LogonHandler | ISO logon OK ولی inject PED ناموفق — rc=96: ${error.message}",
                error,
            )
            BpLogonResult(
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
        request: BpLogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpLogonResult {
        return BpLogonResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.LOGON,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.CONNECT_FAILED,
                responseMessage = transport.connectFailedMessage(error),
            ),
        )
    }

    override fun sendFailure(
        request: BpLogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpLogonResult {
        return BpLogonResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.LOGON,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.SEND_FAILED,
                responseMessage = transport.sendFailedMessage(error),
            ),
        )
    }

    override fun receiveFailure(
        request: BpLogonRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpLogonResult {
        return BpLogonResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.LOGON,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.RECEIVE_FAILED,
                responseMessage = transport.receiveFailedMessage(error),
            ),
        )
    }

    override fun networkError(
        request: BpLogonRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpLogonResult = receiveFailure(request, sentMessage, e)
}
