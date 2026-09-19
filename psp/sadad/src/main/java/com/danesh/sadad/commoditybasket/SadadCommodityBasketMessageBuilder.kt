package com.danesh.sadad.commoditybasket

import com.danesh.api.TransactionContextProvider
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadCommodityBasketMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    private fun commonFields(mti: String, processingCode: String): IsoMessage {
        val clock = contextProvider.currentClock()
        return messageProvider.create().apply {
            this.mti = mti
            this.processingCode = processingCode
            stan = messageSupport.nextStan()
            getIsoMessage().set(12, clock.time)
            getIsoMessage().set(13, clock.date.drop(4))
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    fun buildInquiry(request: SadadCommodityBasketInquiryRequest): IsoMessage {
        val data = request.commodityListData
        val field63 = "01" + "046" + data.length.toString().padStart(3, '0') + data
        return commonFields(
            SadadKeyConfig.INQUIRY_COMMODITY_BASKET_MTI,
            SadadKeyConfig.COMMODITY_BASKET_PROCESSING_CODE,
        ).apply {
            privateUseField63 = field63
        }
    }

    fun buildSale(request: SadadCommodityBasketSaleRequest): IsoMessage {
        val data = request.transactionAmount.filter { it.isDigit() }.padStart(12, '0').takeLast(12) +
            request.creditAmount.filter { it.isDigit() }.padStart(12, '0').takeLast(12) +
            request.traceItem.filter { it.isDigit() }.padStart(13, '0').takeLast(13)
        val field63 = "01" + "047" + data.length.toString().padStart(3, '0') + data
        return commonFields(
            SadadKeyConfig.SALE_COMMODITY_BASKET_MTI,
            SadadKeyConfig.COMMODITY_BASKET_PROCESSING_CODE,
        ).apply {
            privateUseField63 = field63
        }
    }

    fun buildCancel(request: SadadCommodityBasketCancelRequest): IsoMessage {
        val orderTraceId = request.orderTraceId.filter { it.isDigit() }.padStart(13, '0').takeLast(13)
        val field63 = "01" + "058" + orderTraceId.length.toString().padStart(3, '0') + orderTraceId
        return commonFields(
            SadadKeyConfig.CANCEL_COMMODITY_BASKET_MTI,
            SadadKeyConfig.COMMODITY_BASKET_PROCESSING_CODE,
        ).apply {
            privateUseField63 = field63
        }
    }
}
