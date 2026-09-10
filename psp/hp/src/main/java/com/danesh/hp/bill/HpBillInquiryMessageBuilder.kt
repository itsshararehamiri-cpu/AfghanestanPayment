package com.danesh.hp.bill

import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig.ASYCUDA
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HpBillInquiryMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: HpBillInquiryRequest): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.BILL_INQUIRY
        val billNumber = request.billId.trim().take(7)
        val serviceId = request.payId.trim().take(8)
        val serviceNumber = serviceId.padStart(8, '0').takeLast(8)

        return messageProvider.create().apply {
            mti = profile.mti
            processingCode = profile.processingCode
            // DE11 — مثل بقیه تراکنش‌های HP
            stan = messageSupport.nextStan()
            // DE2 — PAN از کارت‌کشی قبل از استعلام
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = ZERO_AMOUNT
            // DE12 — مثل بقیه تراکنش‌های HP
            dateTime = session.dateTime
            // DE22 (+ DE41/DE42) — مثل بقیه تراکنش‌های HP؛ بدون DE24
         //   messageSupport.run { applyHpStandardTerminalFields() }
            currency = session.currency
            // بدون DE35 Track 2
            mac = profile.emptyMac
//            setField48 {
//                setTransactionType("700")
//                setTerminalType("2")
//                setCard2NNumber(request.destinationAccount)
//                setFinancialTransactionIndicator("1")
//            }
            messageSupport.run { applyHpAcceptorIds() }
            setField48 {
                setTransactionType(FUNCTION_CODE) // 002
                setTerminalType(TERMINAL_TYPE_POS) // 012
                setField48Tag("033",ASYCUDA ) // 033 Service Identifier
                setFinancialTransactionIndicator(NON_FINANCIAL) // 040
                setField48Tag("044", serviceNumber) // 044 Service Number
                setField48Tag("850", billNumber) // 850 Bill Number
                setField48Tag("856", serviceId) // 856 Service ID
            }
        }
    }

    companion object {
        private const val FUNCTION_CODE = "511"
        private const val TERMINAL_TYPE_POS = "2"
        private const val NON_FINANCIAL = "0"
        private const val ZERO_AMOUNT = "000000000000"
    }
}
