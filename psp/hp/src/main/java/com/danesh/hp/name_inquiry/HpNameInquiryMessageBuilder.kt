//package com.danesh.hp.name_inquiry
//
//import android.util.Log
//import com.danesh.api.NameInquiryRequest
//import com.danesh.api.TransactionIsoProfile
//import com.danesh.hp.iso.HpIsoMessageSupport
//import com.danesh.hp.key.HpKeyConfig
//import com.danesh.iso.IsoMessage
//import com.danesh.iso.IsoMessageProvider
//import javax.inject.Inject
//import javax.inject.Singleton
//
///**
// * Cardholder / Wallet Holder Name Inquiry (همراه‌پی)
// * MTI 1100 / DE3 350000 / Function Code 651 / Amount 0
// * کارت: DE2 = destination PAN
// * کیف‌پول: DE2 = source PAN + DE48 تگ 045
// */
//@Singleton
//class HpNameInquiryMessageBuilder @Inject constructor(
//    private val messageSupport: HpIsoMessageSupport,
//    private val messageProvider: IsoMessageProvider,
//) {
//    fun build(request: NameInquiryRequest): IsoMessage {
//        val session = messageSupport.beginSession()
//        val profile = TransactionIsoProfile.NAME_INQUIRY
//        val destination = request.destination.filter { it.isDigit() }
//
//        return messageProvider.create().apply {
//            mti = profile.mti
//            processingCode = profile.processingCode
//            Log.d("TAG", "build: ddddddddd${request.pan}")
//            Log.d("TAG", "build: dddddddddd${request}")
//
//            stan = messageSupport.nextStan()
//            pan = destination//request.sourceWallet.filter { it.isDigit() }.take(16)//
//                /*destination.take(16)
//                when {
//                request.forWalletToWallet -> request.sourceWallet.filter { it.isDigit() }.take(16)
//                request.forWallet -> messageSupport.resolvePan(request.pan, request.track2)
//                else -> destination.take(16)
//            }*/
//            amount = "0"
//            dateTime = session.dateTime
//           // messageSupport.run { applyHpStandardTerminalFields() }
//         //   messageSupport.run { applyHpFunctionCode(profile) }
//            terminalId="12345678"//06493050
//            merchantId="HPA000400300200"//"44236789"
//            mcc="2609"
//            mac = profile.emptyMac
//           // setRrn(session.dateTime)
//            Log.d("TAG", "buildd: ddd${request.forWalletToWallet}")
//            Log.d("TAG", "build: ddd${request.forWallet}")
//            setField48 {
//                setTransactionType(FUNCTION_CODE)
//                setCard2NNumber("9004236218492037")// TODO:
//           //     setCard2NNumber(destination.take(16))
//            }// TODO:
////            when {
////                request.forWalletToWallet -> {
////                    setField48 {
////                        setTransactionType(FUNCTION_CODE)
////                        setCard2NNumber(destination.take(16))
////                    }
////                }
////                request.forWallet -> {
////                    setField48 {
////                        setTransactionType(FUNCTION_CODE)
////                        setField48Tag(WALLET_TAG, destination.take(8))
////                    }
////                }
////            }
//        }
//    }
//
//    companion object {
//        private const val FUNCTION_CODE = "651"
//        private const val WALLET_TAG = "045"
//        private const val ZERO_AMOUNT = "000000000000"
//    }
//}
package com.danesh.hp.name_inquiry

import android.util.Log
import com.danesh.api.NameInquiryRequest
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig
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
            Log.d("TAG", "build: ddddddddd${request.pan}")
            Log.d("TAG", "build: dddddddddd${request}")

            stan = messageSupport.nextStan()
            pan = "9004236218492037"//destination//request.sourceWallet.filter { it.isDigit() }.take(16)//
            /*destination.take(16)
            when {
            request.forWalletToWallet -> request.sourceWallet.filter { it.isDigit() }.take(16)
            request.forWallet -> messageSupport.resolvePan(request.pan, request.track2)
            else -> destination.take(16)
        }*/
            amount = "0"
            dateTime = session.dateTime
            // messageSupport.run { applyHpStandardTerminalFields() }
            //   messageSupport.run { applyHpFunctionCode(profile) }
            messageSupport.run { applyHpAcceptorIds() }
            mcc="2609"
            mac = profile.emptyMac
            // setRrn(session.dateTime)
            Log.d("TAG", "buildd: ddd${request.forWalletToWallet}")
            Log.d("TAG", "build: ddd${request.forWallet}")
            setField48 {
                setTransactionType(FUNCTION_CODE)
                //setCard2NNumber("9004236218492037")// TODO:
                //     setCard2NNumber(destination.take(16))
            }// TODO:
//            when {
//                request.forWalletToWallet -> {
//                    setField48 {
//                        setTransactionType(FUNCTION_CODE)
//                        setCard2NNumber(destination.take(16))
//                    }
//                }
//                request.forWallet -> {
//                    setField48 {
//                        setTransactionType(FUNCTION_CODE)
//                        setField48Tag(WALLET_TAG, destination.take(8))
//                    }
//                }
//            }
        }
    }

    companion object {
        private const val FUNCTION_CODE = "651"
        private const val WALLET_TAG = "045"
        private const val ZERO_AMOUNT = "000000000000"
    }
}
