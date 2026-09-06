package com.danesh.sadad.topup

import com.danesh.api.TopUpUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * شارژ مستقیم سداد (13-TOPUP سند) — درخواست به سوئیچ.
 *
 * MTI 0200 / DE3 230000 / DE22 021 / DE24 007 (NII) / DE25 14.
 * سند فیلد 48 را برای این تراکنش تعریف نکرده؛ اپراتور/شمارهٔ موبایل با قالب مستند‌شدهٔ DE63
 * (Count n2 + [FunctionCode n3 + #len n3 + Data] تکرارشونده) حمل می‌شوند.
 */
@Singleton
class SadadTopUpMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: TopUpUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val amount = request.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val operator = request.operatorCode.trim()
        val mobile = request.mobileNumber.filter { it.isDigit() }
        return messageProvider.create().apply {
            mti = SadadKeyConfig.TOPUP_MTI
            processingCode = SadadKeyConfig.TOPUP_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            this.amount = amount
            dateTime = session.dateTime
            messageSupport.run { applySadadStandardTerminalFields() }
            pointOfServiceEntryMode = SadadKeyConfig.ISO_POS_ENTRY_MODE
            nii = SadadKeyConfig.NII
            messageReasonCode = SadadKeyConfig.POS_CONDITION_CODE
            currency = session.currency
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            getIsoMessage().set(63, buildFunctionCodeEnvelope(operator, mobile))
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    private fun buildFunctionCodeEnvelope(operator: String, mobile: String): String {
        val entries = listOf(
            SadadKeyConfig.TOPUP_OPERATOR_FUNCTION_CODE to operator,
            SadadKeyConfig.TOPUP_MOBILE_FUNCTION_CODE to mobile,
        ).filter { (_, data) -> data.isNotBlank() }
        val sb = StringBuilder()
        sb.append(entries.size.toString().padStart(2, '0'))
        entries.forEach { (functionCode, data) ->
            sb.append(functionCode)
            sb.append(data.length.toString().padStart(3, '0'))
            sb.append(data)
        }
        return sb.toString()
    }
}
