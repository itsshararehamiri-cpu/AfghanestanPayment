package com.danesh.hp.config


import android.util.Log
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.key.HpKeyConfig
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import com.danesh.iso.field48.HpField48Tlv
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalConfigHandler @Inject constructor(
    private val terminalConfigMessageMessageBuilder: HpTerminalConfigMessageHandler,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
    private val configurationStore: DeviceConfigurationStore,
    private val deviceOperations: PspDeviceOperations,
    private val terminalConfigStore: HpTerminalConfigStore,
    private val contextProvider: TransactionContextProvider,
) : HandlerTransaction<HpTerminalConfigRequest, HpTerminalConfigResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpTerminalConfigRequest): IsoMessage {
        Log.d("TAG", "buildMessage: dddddddddrt")
        Log.d("TAG", "buildMessage: dddddddddddddnooor")
        return terminalConfigMessageMessageBuilder.build()
    }

    override fun queueFailure(request: HpTerminalConfigRequest): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooora")

        val message = buildMessage(request)
        return HpTerminalConfigResult(
            detail = transport.map(
                transactionType = TransactionType.TERMINAL_CONFIG,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
                masterKey = "",
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        Log.d("TAG", "buildMessage: dddddddddddddnooorb")
        response?.getIsoMessage()?.dump(System.out, "oooo")
        return (response?.responseCode != "300")
    }
    // IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooord")

        /** DE39=302 — بخش 9.3 مستند: پایانه نزد کارن پیکربندی نشده (not provisioned). */
        val responseMessage = if (response?.responseCode == RESPONSE_CODE_NOT_PROVISIONED) {
            messages.notProvisioned()
        } else {
            messages.failed()
        }
        return HpTerminalConfigResult(
            detail = transport.map(
                transactionType = TransactionType.TERMINAL_CONFIG,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = responseMessage,
                masterKey = "",
            ),
        )
    }

    override fun success(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorg")

        return try {
            if (response != null && !hasValidConfigurationPayload(response)) {
                // ساختار/تگ‌های الزامی DE72 پاسخ نامعتبرند — بخش 9.3: قبل از فعال‌سازی
                // باید ساختار پاسخ، تگ‌های الزامی و هش اعتبارسنجی شوند. پیکربندی فعلی
                // دست‌نخورده می‌ماند و درخواست در انتظار باقی می‌ماند تا تلاش بعدی 1305 شود.
                return HpTerminalConfigResult(
                    detail = transport.failureDetail(
                        transactionType = TransactionType.TERMINAL_CONFIG,
                        sentMessage = sentMessage,
                        response = response,
                        responseCode = RESPONSE_CODE_INVALID_PAYLOAD,
                        responseMessage = messages.failed(),
                    ),
                )
            }

            configurationStore.markConfigured()
            terminalConfigStore.markActivated()
            response?.let(::persistTerminalConfig)
            terminalConfigStore.clearPendingRequest()
            HpTerminalConfigResult(
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
            HpTerminalConfigResult(
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

    /**
     * بخش 9.3 مستند KAREN: قبل از فعال‌سازی پیکربندی، POS باید ساختار پاسخ، تگ‌های
     * الزامی، و هش را اعتبارسنجی کند. فرمول دقیق هش پاسخ (تگ 011) در مستند وایر مشخص
     * نشده؛ در نبود آن، حداقلِ اعتبارسنجی ساختاری را انجام می‌دهیم: DE26/DE41/DE42 و
     * تگ 011 باید حاضر باشند و تگ 011 باید یک SHA-256 هگزادسیمال ۶۴ نویسه‌ای باشد.
     */
    private fun hasValidConfigurationPayload(response: IsoMessage): Boolean {
        val iso = response.getIsoMessage()
        if (iso.getString(26)?.isNotBlank() != true) return false
        if (iso.getString(41)?.isNotBlank() != true) return false
        if (iso.getString(42)?.isNotBlank() != true) return false

        val f72 = response.f72
        if (f72.isBlank()) return false
        val tags = HpField48Tlv().apply { unpack(f72) }
        val hash = tags.getNode(TAG_RESPONSE_HASH).orEmpty()
        return SHA256_HEX_REGEX.matches(hash)
    }

    override fun connectFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorv")

        return HpTerminalConfigResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.INIT,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.CONNECT_FAILED,
                responseMessage = transport.connectFailedMessage(error),
            ),
        )
    }

    override fun sendFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorm")

        return HpTerminalConfigResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.TERMINAL_CONFIG,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.SEND_FAILED,
                responseMessage = transport.sendFailedMessage(error),
            ),
        )
    }

    override fun receiveFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorq")

        return HpTerminalConfigResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.INIT,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.RECEIVE_FAILED,
                responseMessage = transport.receiveFailedMessage(error),
            ),
        )
    }

    override fun networkError(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorm")

        return receiveFailure(request, sentMessage, e)
    }

    /**
     * طبق بخش 9.3 مستند پروتکل: به‌روزرسانی کامل هویت پایانه از DE41/DE42/DE43 پاسخ 1314
     * به دست می‌آید. مقادیر خالی، مقدار فعلاً ذخیره‌شده را حفظ می‌کنند تا در جاهای دیگر
     * پروژه (ساخت پیام‌های تراکنشی و صفحه پیکربندی پایانه) از آخرین اطلاعات معتبر استفاده شود.
     */
    private fun persistTerminalConfig(response: IsoMessage) {
        val current = contextProvider.getTerminalConfig()
        val terminalId = response.terminalId.trim().ifBlank { current.terminalId }
        val merchantId = response.merchantId.trim().ifBlank { current.merchantId }
        val merchantNameLocation = response.getIsoMessage().getString(43)?.trim().orEmpty()
        // DE26 — بخش 3 مستند: پروفایل فعال باید MCC را برای سایر تراکنش‌های مالی فراهم کند.
        val mcc = response.getIsoMessage().getString(26)?.trim().orEmpty()
        // DE72 خام — بخش 9.3: شامل تگ‌های پیکربندی (مثلاً 020/ارز، 030-031/هدر-فوتر رسید،
        // 040/فهرست تراکنش‌های فعال) که در محل مصرف با HpField48Tlv خوانده می‌شوند.
        val configPayload = response.f72.trim()
        val updated = current.copy(
            terminalId = terminalId,
            merchantId = merchantId,
            merchantName = merchantNameLocation.ifBlank { current.merchantName },
            mcc = mcc.ifBlank { current.mcc },
            configPayload = configPayload.ifBlank { current.configPayload },
        )
        if (updated != current) {
            contextProvider.saveTerminalConfig(updated)
        }
    }

    companion object {
        /** DE39=302 — بخش 9.3 مستند: پایانه نزد کارن پیکربندی نشده (not provisioned). */
        private const val RESPONSE_CODE_NOT_PROVISIONED = "302"

        /** کد داخلی برای پاسخ 1314 که ساختار/تگ‌های الزامی یا هشش نامعتبر است. */
        private const val RESPONSE_CODE_INVALID_PAYLOAD = "97"

        private const val TAG_RESPONSE_HASH = "011"
        private val SHA256_HEX_REGEX = Regex("^[0-9A-Fa-f]{64}$")
    }
}
