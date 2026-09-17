package com.danesh.bp.coupon.purchase

import com.danesh.api.CouponPurchaseUserInput
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.field22.BpPosEntryMode
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpCouponPurchaseMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
) {

    /**
     * فیلد 4 باید برابر با فیلد 6 پاسخ تراکنش استعلام کالابرگ ([request.amount] که از
     * TransactionResultDetail.couponCashAmount مرحله‌ی استعلام تأمین شده) باشد. فیلد 37 و
     * فیلد 44 نیز باید دقیقاً برابر با فیلد 37 و فیلد 44 همان پاسخ استعلام باشند.
     */
    suspend fun build(request: CouponPurchaseUserInput): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.COUPON_PURCHASE_MTI
            processingCode = BpKeyConfig.COUPON_PURCHASE_PROCESSING_CODE
            amount = formatIsoAmount(request.amount)
            stan = contextProvider.nextStan()
            dateTime = currentLocalDateTime()
            pointOfServiceEntryMode = BpPosEntryMode.forCurrentCardRead()
            messageReasonCode = BpKeyConfig.PURCHASE_MESSAGE_REASON
            track2 = request.track2
            setRrn(request.inquiryRrn)
            terminalId = config.terminalId
            additionalResponseData = request.couponTrackingNumber
            currency = BpKeyConfig.COUPON_PURCHASE_CURRENCY
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {
                setField48Tag(
                    tag = BpKeyConfig.COUPON_FIELD48_TAG_LAST_SUCCESS,
                    value = lastSuccessValues.stanTagValue(),
                )
            }
        }
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun formatIsoAmount(amount: String): String {
        val digits = amount.filter(Char::isDigit)
        return digits.padStart(12, '0').takeLast(12)
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
