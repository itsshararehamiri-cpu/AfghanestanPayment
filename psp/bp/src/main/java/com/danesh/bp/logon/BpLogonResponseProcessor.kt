package com.danesh.bp.logon

import android.util.Log
import com.danesh.api.PspLogonWorkingKeys
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.bootstrap.BpBootstrapResponseValidator
import com.danesh.bp.init.wipe
import com.danesh.bp.field48.BpField48Tags
import com.danesh.bp.field48.BpMerchantInfoPersister
import com.danesh.bp.support.SupportMenuResponsePersister
import com.danesh.iso.IsoMessage
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpLogonResponseProcessor @Inject constructor(
    private val responseValidator: BpBootstrapResponseValidator,
    private val contextProvider: TransactionContextProvider,
    private val supportMenuPersister: SupportMenuResponsePersister,
    private val merchantInfoPersister: BpMerchantInfoPersister,
) {

    suspend fun processSuccess(
        requestMessage: IsoMessage,
        response: IsoMessage
    ): PspLogonWorkingKeys {
        response.print(">>lo")
        response.getDump()
        response.getIsoMessage().dump(System.out,"PPPO>")
        BpLogonTrace.step("ResponseProcessor", "اعتبارسنجی MAC و فیلدهای همبستگی")
        responseValidator.validateLogonResponse(
            requestStan = requestMessage.stan,
            response = response,
        )
        persistTerminalIds(response)
        merchantInfoPersister.persistFromResponse(response)
        logField48Tag23(response)
        supportMenuPersister.persistFromResponse(response)
        return extractWorkingKeys(response)
    }


    fun extractWorkingKeys(response: IsoMessage): PspLogonWorkingKeys {
        val field62 = (
                response.privateUseField62Bytes()
                    ?: response.privateUseField62.toByteArray(Charsets.ISO_8859_1)
                ).copyOf()
        check(field62.isNotEmpty()) { "فیلد 62 (working keys) در پاسخ logon خالی است" }
        BpLogonTrace.step("F62", "received bytes=${field62.size}")
        return try {
            BpLogonField62Parser.parse(field62).also { keys ->
                BpLogonTrace.step(
                    "ResponseProcessor",
                    "F62 parsed blockSize=${keys.encryptedPinKey.size}",
                )
            }
        } finally {
            field62.wipe()
        }
    }

    private fun logField48Tag23(response: IsoMessage) {
        response.unpackField48()
        val tag23 = response.getField48Tag(BpField48Tags.SUPPORT_TITLES_AMOUNTS)
        response.print("iiiu>")
        Log.d("TAG", "logField4kk8Tag23: ddddddddd$tag23")
        BpLogonTrace.step(
            "ResponseProcessor",
            "F48 tag23 (SUPPORT_TITLES_AMOUNTS)=${tag23 ?: "(missing)"}",
        )
    }

    private fun persistTerminalIds(response: IsoMessage) {
        val current = contextProvider.getTerminalConfig()
        val terminalId = response.terminalId.takeIf { it.isNotBlank() } ?: current.terminalId
        val merchantId = response.merchantId.takeIf { it.isNotBlank() } ?: current.merchantId
        if (terminalId != current.terminalId || merchantId != current.merchantId) {
            contextProvider.saveTerminalConfig(
                current.copy(
                    terminalId = terminalId,
                    merchantId = merchantId,
                ),
            )
        }
    }
}
