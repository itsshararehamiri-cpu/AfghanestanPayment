package com.danesh.sadad.bill

import com.danesh.api.BillInquiryRequest
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadBillInquiryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: BillInquiryRequest): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.BILL_INQUIRY
        val billNumber = request.billId.trim().take(7)
        val serviceId = request.payId.trim().take(8)
        val serviceNumber = serviceId.padStart(8, '0').takeLast(8)
        return messageProvider.create().apply {
            messageSupport.run {
                applySadadCardFields(
                    profile = profile,
                    session = session,
                    pan = request.pan,
                    amount = ZERO_AMOUNT,
                    track2 = request.track2,
                    pinBlock = "",
                    includeTrack2 = false,
                )
            }
            setField48 {
                setTransactionType(FUNCTION_CODE)
                setTerminalType(TERMINAL_TYPE_POS)
                setField48Tag("033", SadadKeyConfig.ASYCUDA)
                setFinancialTransactionIndicator(NON_FINANCIAL)
                setField48Tag("044", serviceNumber)
                setField48Tag("850", billNumber)
                setField48Tag("856", serviceId)
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
