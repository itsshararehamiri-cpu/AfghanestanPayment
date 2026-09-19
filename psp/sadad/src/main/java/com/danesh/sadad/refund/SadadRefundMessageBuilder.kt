package com.danesh.sadad.refund

import com.danesh.api.TransactionContextProvider
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/** 20-REFUND: MTI 0200 (پاسخ 0210) / DE3 200000. */
@Singleton
class SadadRefundMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadRefundRequest): IsoMessage {
        val clock = contextProvider.currentClock()
        val amount = messageSupport.formatIsoAmount(request.amount)
        val reference = request.reference.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val originalStan = request.originalStan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val refundType = request.refundType.filter { it.isDigit() }.padStart(2, '0').takeLast(2)
        val field61 = refundType + reference + amount + originalStan
        return messageProvider.create().apply {
            mti = SadadKeyConfig.REFUND_MTI
            processingCode = SadadKeyConfig.REFUND_PROCESSING_CODE
            this.amount = amount
            stan = messageSupport.nextStan()
            getIsoMessage().set(12, clock.time)
            getIsoMessage().set(13, clock.date.drop(4))
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField61 = field61
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
