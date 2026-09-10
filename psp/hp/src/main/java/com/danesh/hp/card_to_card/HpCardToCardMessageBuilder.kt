//package com.danesh.hp.card_to_card
//
//import android.util.Log
//import com.danesh.api.CardToCardUserInput
//import com.danesh.api.TransactionIsoProfile
//import com.danesh.hp.iso.HpIsoMessageSupport
//import com.danesh.hp.key.HpKeyConfig
//import com.danesh.iso.IsoMessage
//import com.danesh.iso.IsoMessageProvider
//import org.jpos.iso.ISOUtil
//import javax.inject.Inject
//import javax.inject.Singleton
//
///**
// * Card-to-Card Transfer (همراه‌پی)
// * MTI 1100 / DE3 500000 / Function Code 689
// * DE48: 002=689 ، 021=Destination PAN ، 049=Destination Cardholder Name (از Name Inquiry)
// * DE37: RRN استعلام نام قبلی (در صورت موجود بودن)
// */
//@Singleton
//class HpCardToCardMessageBuilder @Inject constructor(
//    private val messageSupport: HpIsoMessageSupport,
//    private val messageProvider: IsoMessageProvider,
//) {
//    fun build(request: CardToCardUserInput): IsoMessage {
//        val session = messageSupport.beginSession()
//        val profile = TransactionIsoProfile.CARD_TO_CARD
//        val destinationPan = request.destinationPan.filter { it.isDigit() }.take(16)
//        val track2 = messageSupport.normalizeTrack2(request.track2)
//        val rrn = request.rrn.trim().ifBlank { session.dateTime }
//
//       return messageProvider.create().apply {
//            mti = profile.mti
//            processingCode = profile.processingCode
//            stan = messageSupport.nextStan()
//            pan ="9004230100000027"//" messageSupport.resolvePan(request.pan, request.track2)
//            amount = request.amount
//            dateTime = session.dateTime
//         //   messageSupport.run { applyHpStandardTerminalFields() }
//            pointOfServiceEntryMode = HpKeyConfig.CARD_TO_CARD_POS_ENTRY_MODE
//           // messageSupport.run { applyHpFunctionCode(profile) }
//            currency = session.currency.ifBlank { HpKeyConfig.CARDHOLDER_BILLING_CURRENCY }
//          //  tt51 = "971"
//            //HpKeyConfig.CARDHOLDER_BILLING_CURRENCY
//            if (track2.isNotBlank()) {
//                this.track2 = track2
//            }
//            pinBlock = ISOUtil.hex2byte(request.pinBlock)
//            mac = profile.emptyMac
//          //  setRrn(rrn)
//          //  messageSupport.run { applyHpTransferAcquirerFields(track2) }
//            val holderName = request.holderName.trim()
//           terminalId="12345678"//06493050
//           merchantId="HPA000400300200"//"44236789"
//            setField48 {
//                setTransactionType(FUNCTION_CODE)
//                setCard2NNumber("9004230100000016")
//                if (holderName.isNotBlank()) {
//                    setField48Tag(HOLDER_NAME_TAG, holderName)
//                }
//            }
//
//        }
//
//    }
//
//
//    companion object {
//        private const val FUNCTION_CODE = "689"
//        private const val HOLDER_NAME_TAG = "049"
//    }
//}
package com.danesh.hp.card_to_card

import android.util.Log
import com.danesh.api.CardToCardUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Card-to-Card Transfer (همراه‌پی)
 * MTI 1100 / DE3 500000 / Function Code 689
 * DE48: 002=689 ، 021=Destination PAN ، 049=Destination Cardholder Name (از Name Inquiry)
 * DE37: RRN استعلام نام قبلی (در صورت موجود بودن)
 */
@Singleton
class HpCardToCardMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CardToCardUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.CARD_TO_CARD
        val destinationPan = request.destinationPan.filter { it.isDigit() }.take(16)
        val track2 = messageSupport.normalizeTrack2(request.track2)
        val rrn = request.rrn.trim().ifBlank { session.dateTime }

        return messageProvider.create().apply {
            mti = profile.mti
            processingCode = profile.processingCode
            stan = messageSupport.nextStan()
            pan ="9004230100000027"//" messageSupport.resolvePan(request.pan, request.track2)
            amount = request.amount
            dateTime = session.dateTime
            //   messageSupport.run { applyHpStandardTerminalFields() }
            pointOfServiceEntryMode = HpKeyConfig.CARD_TO_CARD_POS_ENTRY_MODE
            // messageSupport.run { applyHpFunctionCode(profile) }
            currency = session.currency.ifBlank { HpKeyConfig.CARDHOLDER_BILLING_CURRENCY }
            //  tt51 = "971"
            //HpKeyConfig.CARDHOLDER_BILLING_CURRENCY
            if (track2.isNotBlank()) {
                this.track2 = track2
            }
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = profile.emptyMac
            //  setRrn(rrn)
            //  messageSupport.run { applyHpTransferAcquirerFields(track2) }
            val holderName = request.holderName.trim()
            messageSupport.run { applyHpAcceptorIds() }
            setField48 {
                setTransactionType(FUNCTION_CODE)
                setCard2NNumber("9004230100000016")
                if (holderName.isNotBlank()) {
                    setField48Tag(HOLDER_NAME_TAG, holderName)
                }
            }

        }

    }


    companion object {
        private const val FUNCTION_CODE = "689"
        private const val HOLDER_NAME_TAG = "049"
    }
}
