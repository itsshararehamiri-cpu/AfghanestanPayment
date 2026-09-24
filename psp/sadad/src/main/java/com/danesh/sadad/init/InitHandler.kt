package com.danesh.sadad.init

import android.util.Log
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitRequest
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadNetworkResult
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.util.Field63Parser
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
    private val contextProvider: TransactionContextProvider,
    private val initProfileStore: SadadInitProfileStore,
) : HandlerTransaction<InitRequest, SadadNetworkResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: InitRequest): IsoMessage =
        kotlinx.coroutines.runBlocking { initMessageBuilder.build() }

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
            persistMerchantFromInit(response)
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

    /**
     * پاسخ INIT: DE41/DE42 و فیلد ۶۳ (Private4 / Host Function Code
     * Terminal initializer) شماره پایانه و مشخصات پذیرنده را می‌دهد.
     */
    private fun persistMerchantFromInit(response: IsoMessage?) {
        if (response == null) return
        persistTerminalIds(response)
        val field63 = response.privateUseField63
        if (field63.isBlank()) return
        val initializer = parseTerminalInitializer(field63) ?: run {
            Log.w("INIT", "field 63 present but Terminal initializer could not be parsed")
            return
        }
        Log.d("INIT", "Terminal ID = ${initializer.terminalId}")
        Log.d("INIT", "Acq ID = ${initializer.acqId}")
        Log.d("INIT", "Acq Name Fa = ${initializer.acqNameFa}")
        Log.d("INIT", "Acq Name En = ${initializer.acqNameEn}")
        Log.d("INIT", "Acq Address Fa = ${initializer.acqAddressFa}")
        Log.d("INIT", "Acq Address En = ${initializer.acqAddressEn}")
        Log.d("INIT", "Acq Tel = ${initializer.tel}")
        Log.d("INIT", "Acq Postal = ${initializer.postalCode}")
        Log.d("INIT", "Lines Count = ${initializer.linesCount}")
        Log.d("INIT", "Headline = ${initializer.headlineNo}")
        Log.d("INIT", "Tax Memory Unique = ${initializer.taxMemoryUniqueCode}")
        Log.d("INIT", "Sales Fund Device Serial = ${initializer.salesFundDeviceSerial}")
        Log.d("INIT", "Sales Fund Memory Serial = ${initializer.salesFundMemorySerial}")
        val current = contextProvider.getTerminalConfig()
        contextProvider.saveTerminalConfig(
            current.copy(
                terminalId = initializer.terminalId.ifBlank { current.terminalId },
                merchantId = initializer.acqId.ifBlank { current.merchantId },
                merchantName = initializer.acqNameFa.ifBlank { current.merchantName },
                englishMerchantName = initializer.acqNameEn.ifBlank { current.englishMerchantName },
                merchantAddress = initializer.acqAddressFa.ifBlank { current.merchantAddress },
                merchantPostalCode = initializer.postalCode.ifBlank { current.merchantPostalCode },
                merchantPhone = initializer.tel.ifBlank { current.merchantPhone },
            ),
        )
        val extras = initProfileStore.get()
        initProfileStore.save(
            extras.copy(
                englishMerchantAddress = initializer.acqAddressEn.ifBlank { extras.englishMerchantAddress },
                linesCount = initializer.linesCount.ifBlank { extras.linesCount },
                headlineNo = initializer.headlineNo.ifBlank { extras.headlineNo },
                taxMemoryUniqueCode = initializer.taxMemoryUniqueCode.ifBlank { extras.taxMemoryUniqueCode },
                salesFundDeviceSerial = initializer.salesFundDeviceSerial.ifBlank { extras.salesFundDeviceSerial },
                salesFundMemorySerial = initializer.salesFundMemorySerial.ifBlank { extras.salesFundMemorySerial },
            ),
        )
    }

    private fun parseTerminalInitializer(field63: String): TerminalInitializer? {
        val candidates = linkedSetOf(field63)
        val preferred = runCatching { Field63Parser.parse(field63) }.getOrDefault(emptyList())
            .sortedByDescending { it.code == SadadKeyConfig.FUNCTION_CODE_TERMINAL_INITIALIZER }
        preferred.forEach { block ->
            candidates += block.data
        }
        if (field63.length > 5) candidates += field63.substring(5)
        if (field63.length > 8) candidates += field63.substring(8)
        return candidates.firstNotNullOfOrNull { candidate ->
            SadadTerminalInitializerCodec.parse(candidate)
        }
    }

    /**
     * DE41/DE42 پاسخ INIT — سوئیچ شماره ترمینال/پذیرنده واقعی را برمی‌گرداند
     * (در درخواست فقط مقادیر موقتِ فعال‌سازی ارسال شده بودند)؛ بدون این ذخیره،
     * تراکنش‌های بعدی (از جمله LOGON) با DE41/DE42 خالی ارسال می‌شوند.
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
