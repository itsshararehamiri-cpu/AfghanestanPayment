package com.danesh.sadad.kahroba

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
 * کهربا (NFC / EMV بدون تماس) سداد.
 *
 * ساختار پیام همان خرید/مانده‌ی کارت مغناطیسی است با این تفاوت‌ها:
 * DE22 071  POS Entry Mode (NFC)
 * DE52 PIN block — فقط وقتی PIN گرفته شده باشد (خرید کهربا زیر سقف ممکن است بدون PIN باشد)
 * DE55 داده‌ی EMV کارت (خروجی GENERATE AC) به‌صورت HEX
 */
@Singleton
class SadadKahrobaMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
) {
    /** 21.1-KAHROBA SALE — MTI 0200 / DE3 000000 / DE22 071 */
    suspend fun buildSale(request: SadadKahrobaSaleRequest): IsoMessage {
        messageSupport.beginSession()
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.PURCHASE_MTI
            processingCode = SadadKeyConfig.PURCHASE_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.KAHROBA_POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = kahrobaTrack2Field(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            if (request.pinBlock.isNotBlank()) {
                pinBlock = ISOUtil.hex2byte(request.pinBlock)
            }
            getIsoMessage().set(ICC_DATA_FIELD, request.iccData)
            transportData = messageSupport.initTransportData()
        }
        message.requireSadad().unsetFields(60, 61, 63)
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }

    /** 21.2-KAHROBA BALANCE — MTI 0100 / DE3 310000 / DE22 071 */
    suspend fun buildBalance(request: SadadKahrobaBalanceRequest): IsoMessage {
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.BALANCE_MTI
            processingCode = SadadKeyConfig.BALANCE_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.KAHROBA_POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = kahrobaTrack2Field(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            if (request.pinBlock.isNotBlank()) {
                pinBlock = ISOUtil.hex2byte(request.pinBlock)
            }
            getIsoMessage().set(ICC_DATA_FIELD, request.iccData)
            transportData = messageSupport.initTransportData()
        }
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }

    private companion object {
        const val ICC_DATA_FIELD = 55
    }
}

/**
 * DE35 سداد برای کارت کهربا — دقیقاً با همان قالب کارت مغناطیسی (K9.readCard: "38" + track2)
 * تا بسته‌بندی فیلد (IFB_NUMERIC_RIGHT_F ثابت ۳۹ رقمی) و رفتار سوئیچ یکسان بماند.
 * جداکنندهٔ D در Track2 تگ 57 به '=' تبدیل می‌شود.
 */
internal fun kahrobaTrack2Field(track2: String): String =
    SADAD_TRACK2_PREFIX + track2.trim().replace('D', '=').replace('d', '=')

private const val SADAD_TRACK2_PREFIX = "38"
