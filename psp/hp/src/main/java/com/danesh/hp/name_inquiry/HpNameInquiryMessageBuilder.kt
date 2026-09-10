package com.danesh.hp.name_inquiry

import com.danesh.api.NameInquiryRequest
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cardholder / Wallet Holder Name Inquiry (همراه‌پی)
 * MTI 1100 / DE3 350000 / Function Code 651 / Amount 0
 * کارت: DE2 = destination PAN
 * کیف‌پول: DE2 = source PAN + DE48 تگ 045
 */
@Singleton
class HpNameInquiryMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: NameInquiryRequest): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.NAME_INQUIRY
        val destination = request.destination.filter { it.isDigit() }

        return messageProvider.create().apply {
            mti = profile.mti
            processingCode = profile.processingCode
            stan = messageSupport.nextStan()
            pan = "9004236218492037" //destination
            amount = "0"
            dateTime = session.dateTime
            messageSupport.run { applyHpAcceptorIds() }
            mcc = "2609"
            mac = profile.emptyMac
            setField48 {
                setTransactionType(FUNCTION_CODE)
                //     setCard2NNumber(destination.take(16))
            }
        }
    }

    companion object {
        private const val FUNCTION_CODE = "651"
        private const val WALLET_TAG = "045"
        private const val ZERO_AMOUNT = "000000000000"
    }
}
