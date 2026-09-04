package com.danesh.bp.init

import com.danesh.api.InitRequest
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.bootstrap.BpBootstrapResponseValidator
import com.danesh.bp.field48.BpField48Tags
import com.danesh.bp.field48.BpMerchantInfoPersister
import com.danesh.bp.support.SupportMenuResponsePersister
import com.danesh.iso.IsoMessage
import com.danesh.iso.requireBp
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpInitResponseProcessor @Inject constructor(
    private val rsaSession: BpInitRsaSession,
    private val deviceOperations: PspDeviceOperations,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val responseValidator: BpBootstrapResponseValidator,
    private val supportMenuPersister: SupportMenuResponsePersister,
    private val contextProvider: TransactionContextProvider,
    private val merchantInfoPersister: BpMerchantInfoPersister,
    ) {

    suspend fun processSuccess(
        request: InitRequest,
        requestMessage: IsoMessage,
        response: IsoMessage,
    ) {
        responseValidator.validateInitResponse(
            requestStan = requestMessage.stan,
            response = response,
        )
        val field61 = response.privateUseField61.trim()
        check(field61.isNotBlank()) { "فیلد 61 در پاسخ خالی است" }

        val deviceSerial = metadataProvider.deviceSerial()
        val poa = if (field61.contains("@")) {
            val responseSerial = field61.substringBefore("@").trim()
            check(responseSerial == deviceSerial) { "سریال پاسخ با دستگاه مطابقت ندارد" }
            field61.substringAfter("@").trim()
        } else {
            field61
        }
        check(poa.isNotBlank()) { "PoA در پاسخ موجود نیست" }
        val normalizedPoa = BpInitRsaSession.normalizePoaHex(poa)
        check(
            BpInitRsaSession.verifyPoa(
                ticket2 = request.secondBallotTicket,
                deviceSerial = deviceSerial,
                receivedPoa = normalizedPoa,
            ),
        ) { "اعتبارسنجی PoA ناموفق بود" }
        val encryptedTerminalKey = response.privateUseField62Bytes()
            ?: response.privateUseField62.toByteArray(Charsets.ISO_8859_1)
        check(encryptedTerminalKey.isNotEmpty()) { "فیلد 62 در پاسخ خالی است" }

        // کلید پایانه / F62 / PoR / PoA نباید لاگ یا در حافظهٔ غیر امن بمانند.
        val requestStan = requestMessage.stan.trim()
        BpInitTrace.step("ResponseProcessor", "decryptField62 شروع")
        val decrypted = try {
            rsaSession.decryptField62(encryptedTerminalKey, requestStan)
        } finally {
            encryptedTerminalKey.wipe()
        }
        check(decrypted.isNotEmpty()) { "Field62 decrypt شده خالی است" }
        BpInitTrace.step(
            "ResponseProcessor",
            "decryptField62 موفق — زنجیره RSA/OAEP معتبر است",
        )
        persistVatPercentage(response)
        persistSupportTitleAndAmounts(response)
        merchantInfoPersister.persistFromResponse(response)

        val terminalKey = BpTerminalKeyParser.parse(decrypted)
        try {
            deviceOperations.completeInit(request, terminalKey)
        } finally {
            decrypted.wipe()
            terminalKey.wipe()
            wipeInitSensitiveFields(requestMessage, response)
            clearSession(requestStan)
        }
        supportMenuPersister.persistFromResponse(response)
    }

    private fun wipeInitSensitiveFields(requestMessage: IsoMessage, response: IsoMessage) {
        runCatching { requestMessage.requireBp().unsetFields(61, 62) }
        runCatching { response.requireBp().unsetFields(61, 62) }
    }

    fun clearSession(stan: String? = null) {
        if (stan.isNullOrBlank()) {
            rsaSession.clear()
        } else {
            rsaSession.clearByStan(stan)
        }
    }

    private fun persistVatPercentage(response: IsoMessage) {
        val vatPercentage = response.getField48Tag(BpField48Tags.VAT_PERCENT).orEmpty()
        if (vatPercentage.isNotEmpty()) {
            contextProvider.saveVatPercentage(vatPercentage)
        }
    }
    private fun persistSupportTitleAndAmounts(response: IsoMessage) {
        val supportTitleAndAmounts = response?.getField48Tag("023").orEmpty()
        if (!supportTitleAndAmounts.isNullOrEmpty()) {
//            contextProvider.saveVatPercentage(
//                supportTitleAndAmounts
//            )
        }
    }
}
