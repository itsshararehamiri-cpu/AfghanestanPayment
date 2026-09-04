package com.danesh.bp.util

import android.util.Log
import com.danesh.api.IsoResponseCodes
import com.danesh.api.maskPanForDisplay
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionSessionClock
import com.danesh.api.TransactionType
import com.danesh.api.parseTransactionClockFromField12
import com.danesh.bp.field48.BpField48Tags
import com.danesh.bp.field54.BpField54Parser
import com.danesh.bp.voucher.BpChargePinDecoder
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpTransactionResultMapper @Inject constructor(
    private val sessionClock: TransactionSessionClock,
    private val chargePinDecoder: BpChargePinDecoder,
) {

    fun map(
        transactionType: TransactionType,
        request: IsoMessage,
        response: IsoMessage?,
        isSuccess: Boolean,
        responseMessage: String,
        merchantId: String,
        merchantName: String,
        merchantPhone: String,
        englishMerchantName: String,
        merchantAddress: String,
        merchantPostalCode: String,
        masterKey: String,mobileNumber: String=""
    ): TransactionResultDetail {
        val source = response ?: request
        val rawPan = source.pan.ifBlank { request.pan }
        val maskedPanValue = rawPan.maskPanForDisplay()
        val (date, time) = resolveDateTime(request)
        val responseCode = source.responseCode.orEmpty().trim()
        val resolvedSuccess = when {
            !isSuccess -> false
            response != null -> IsoResponseCodes.isApproved(responseCode)
            else -> isSuccess
        }
        val field54Balances = response
            ?.takeIf { resolvedSuccess }
            ?.let { BpField54Parser.parse(it.additionalAmounts) }
        val actualBalance = field54Balances?.actual?.toDisplayAmount()
        val availableFrom54 = field54Balances?.available?.toDisplayAmount()
        val availableFrom48 = response?.getField48Tag(BpField48Tags.CREDIT_BALANCE)
            ?.takeIf { it.isNotBlank() }
        val availableBalance = availableFrom54
            ?: availableFrom48
            ?: actualBalance
        val hostReceiptText = response?.field55
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val hostReceiptTextSecond = response?.field56
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val resolvedMerchantId = merchantId
            .ifBlank { source.merchantId }
            .ifBlank { request.merchantId }


        val rawChargePin = response?.getField48Tag(BpField48Tags.CHARGE_PIN)
            ?.takeIf { it.isNotBlank() }
            .orEmpty()
        val voucherPin = when {
            transactionType == TransactionType.VOUCHER &&
                resolvedSuccess &&
                rawChargePin.isNotEmpty() ->
            {
                chargePinDecoder.decode(rawChargePin)
            }
            else -> {
                rawChargePin
            }
        }
        val voucherSerial = response?.getField48Tag(BpField48Tags.CHARGE_SERIAL)
            ?.takeIf { it.isNotBlank() }
            .orEmpty()
        val operatorCode = response?.getField48Tag(BpField48Tags.MOBILE_OPERATOR)
            ?.takeIf { it.isNotBlank() }
            ?: request.getField48Tag(BpField48Tags.MOBILE_OPERATOR).orEmpty().trim()
        val payId = request.getField48Tag(BpField48Tags.PAYMENT_ID).orEmpty().trim()

       return TransactionResultDetail(
            isSuccess = resolvedSuccess,
            transactionType = transactionType,
            terminalId = source.terminalId.ifBlank { request.terminalId },
            merchantId = resolvedMerchantId,
            merchantName = merchantName.ifBlank { resolvedMerchantId },
            merchantPhone = merchantPhone,
            englishMerchantName = englishMerchantName.ifBlank { merchantName },
            merchantAddress = merchantAddress,
            merchantPostalCode = merchantPostalCode,
            pan = maskedPanValue,
            maskedPan = maskedPanValue,
            trace = source.stan.ifBlank { request.stan },
            rrn = source.rrn?.takeIf { it.isNotBlank() },
            date = date,
            time = time,
            dateTime = buildDateTime(date, time),
            responseCode = responseCode.ifBlank {
                if (resolvedSuccess) "000" else ""
            },
            responseMessage = responseMessage,
            amount = source.amount.ifBlank { request.amount },
            actualBalance = actualBalance,
            availableBalance = availableBalance,
            hostReceiptText = hostReceiptText,
            hostReceiptTextSecond = hostReceiptTextSecond,
            issuerName = "",
            posCode = source.pointOfServiceEntryMode.ifBlank { request.pointOfServiceEntryMode },
            masterkey = masterKey,
            voucherPin = voucherPin,
            voucherSerial = voucherSerial,
            productCode = operatorCode,
            mobileNumber = mobileNumber,
            voucherMethod =response?.getField48Tag(BpField48Tags.SERVICE_CODE)?:"",
            payId = payId,
        )

    }

    private fun resolveDateTime(request: IsoMessage): Pair<String, String> {
        sessionClock.current()?.let { return it.date to it.time }
        parseTransactionClockFromField12(request.dateTime)?.let { return it.date to it.time }
        return "" to ""
    }

    private fun buildDateTime(date: String, time: String): String = when {
        date.isNotBlank() && time.isNotBlank() -> "$date - $time"
        date.isNotBlank() -> date
        time.isNotBlank() -> time
        else -> ""
    }
}
