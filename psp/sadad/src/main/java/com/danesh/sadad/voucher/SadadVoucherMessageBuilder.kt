package com.danesh.sadad.voucher

import com.danesh.api.VoucherUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * شارژ/ووچر سداد (4-CHARGE سند) — درخواست به سوئیچ.
 *
 * MTI 0200 / DE3 150000 / DE22 021 / DE24 007 (NII) / DE25 14
 * DE48 = ProviderID(4) + CategoryID(2) + Space(0x20) + ChargeCount(2) — قالب خام سند، بدون TLV.
 * اپراتور انتخابی کاربر روی ProviderID نگاشت می‌شود؛ CategoryID/ChargeCount چون در دامنهٔ
 * فعلی ورودی مجزایی ندارند با مقدار پیش‌فرض "یک شارژ از یک دسته" پر می‌شوند.
 */
@Singleton
class SadadVoucherMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: VoucherUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val amount = request.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val providerId = request.operatorCode.filter { it.isDigit() }
            .padStart(SadadKeyConfig.CHARGE_PROVIDER_ID_LENGTH, '0')
            .takeLast(SadadKeyConfig.CHARGE_PROVIDER_ID_LENGTH)
        val field48 = providerId +
            SadadKeyConfig.CHARGE_DEFAULT_CATEGORY_ID +
            " " +
            SadadKeyConfig.CHARGE_DEFAULT_COUNT
        return messageProvider.create().apply {
            mti = SadadKeyConfig.VOUCHER_MTI
            processingCode = SadadKeyConfig.VOUCHER_PROCESSING_CODE
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
            getIsoMessage().set(48, field48)
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
