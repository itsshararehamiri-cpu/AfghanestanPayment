package com.danesh.sadad.voucher

import com.danesh.api.ChargeCatalog
import com.danesh.api.VoucherUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.iso.requireSadad
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CHARGE سداد — خرید اعتبار از اپراتور (MTI 0200 / DE3 150000).
 *
 * درخواست: 3, 4, 11, 22=021, 24=007, 25=14, 35, 41, 42, 48, 52, 59, 64
 * DE48: ProviderID(4) + CategoryID(2) + Space + ChargeCount(2)
 */
@Singleton
class SadadVoucherMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
    private val chargeCatalog: ChargeCatalog,
) {
    suspend fun build(request: VoucherUserInput): IsoMessage {
        messageSupport.beginSession()
        val product = chargeCatalog.findProduct(request.productId)
        val amount = messageSupport.formatIsoAmount(request.amount)
        val field48 = SadadChargeField48.format(
            providerId = product?.providerId ?: request.operatorCode,
            categoryId = product?.categoryId ?: DEFAULT_CATEGORY_ID,
            chargeCount = if (product?.hasCount == false) 0 else DEFAULT_CHARGE_COUNT,
        )
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.VOUCHER_MTI
            processingCode = SadadKeyConfig.VOUCHER_PROCESSING_CODE
            this.amount = amount
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, field48)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            transportData = messageSupport.initTransportData()
        }
        message.requireSadad().unsetFields(60, 61, 63)
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }

    private companion object {
        const val DEFAULT_CATEGORY_ID = "01"
        const val DEFAULT_CHARGE_COUNT = 1
    }
}
