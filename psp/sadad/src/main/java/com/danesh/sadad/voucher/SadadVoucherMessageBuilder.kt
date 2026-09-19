package com.danesh.sadad.voucher

import com.danesh.api.VoucherUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadVoucherMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    /**
     * 5-CHARGE (MTI 0200/0210, DE3 150000): خرید شارژ از اپراتور موبایل.
     * DE48 طبق سند باید ProviderID(4) + CategoryID(2) + Space(0x20) + ChargeCount(2)
     * باشد؛ مدل فعلی [VoucherUserInput] فقط operatorCode دارد و category/charge-count
     * را در اختیار ندارد — تا تکمیل مدل ورودی، operatorCode به‌عنوان ProviderID و
     * مقادیر پیش‌فرض «۰۱» برای CategoryID/ChargeCount استفاده می‌شود.
     */
    fun build(request: VoucherUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val amount = request.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val providerId = request.operatorCode.filter { it.isDigit() }.padStart(4, '0').takeLast(4)
        val field48 = providerId + DEFAULT_CATEGORY_ID + " " + DEFAULT_CHARGE_COUNT
        return messageProvider.create().apply {
            mti = SadadKeyConfig.VOUCHER_MTI
            processingCode = SadadKeyConfig.VOUCHER_PROCESSING_CODE
            stan = messageSupport.nextStan()
            this.amount = amount
            dateTime = session.dateTime
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            messageSupport.run { applySadadStandardTerminalFields() }
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            getIsoMessage().set(48, field48)
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    private companion object {
        const val DEFAULT_CATEGORY_ID = "01"
        const val DEFAULT_CHARGE_COUNT = "01"
    }
}
