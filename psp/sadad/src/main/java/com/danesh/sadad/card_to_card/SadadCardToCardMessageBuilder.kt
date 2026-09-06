package com.danesh.sadad.card_to_card

import com.danesh.api.CardToCardUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * انتقال کارت‌به‌کارت سداد (10-CARD TO CARD TRANSFER سند) — درخواست به سوئیچ.
 *
 * MTI 0200 / DE3 320000 / DE22 100010100131 (بدون Track2) / DE24 007 (NII) / DE25 14.
 * DE48 = Doer Bank BIN + Destination PAN، DE52 = PIN2، DE54 = ExpiredDate، DE60 = CVV2.
 *
 * سند علاوه‌براین یک گام مجزای «9-CARD TO CARD TRANSFER AUTHORIZATION» (MTI 0100 / DE3 320000)
 * برای تأیید کارت مقصد پیش از انتقال تعریف کرده که فعلاً در این جریان پیاده‌سازی نشده است.
 */
@Singleton
class SadadCardToCardMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CardToCardUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val destinationPan = request.destinationPan.filter { it.isDigit() }.take(16)
        val track2 = messageSupport.normalizeTrack2(request.track2)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.CARD_TO_CARD_TRANSFER_MTI
            processingCode = SadadKeyConfig.CARD_TO_CARD_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.CARD_TO_CARD_POS_ENTRY_MODE
            nii = SadadKeyConfig.NII
            messageReasonCode = SadadKeyConfig.POS_CONDITION_CODE
            pan = messageSupport.resolvePan(request.pan, request.track2)
            dateTime = session.dateTime
            messageSupport.run { applySadadStandardTerminalFields() }
            currency = session.currency.ifBlank { SadadKeyConfig.CARDHOLDER_BILLING_CURRENCY }
            if (request.pinBlock.isNotBlank()) {
                pinBlock = ISOUtil.hex2byte(request.pinBlock)
            }
            messageSupport.run { applySadadTransferAcquirerFields(track2) }
            // Doer Bank BIN: بانک صادرکنندهٔ کارت مقصد، برگرفته از ۶ رقم اول شماره کارت مقصد.
            val doerBankBin = destinationPan.take(BIN_LENGTH)
            getIsoMessage().set(48, doerBankBin + destinationPan)
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    private companion object {
        const val BIN_LENGTH = 6
    }
}
