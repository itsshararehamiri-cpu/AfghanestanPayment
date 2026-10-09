package com.danesh.sadad.commoditybasket

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.iso.requireSadad
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/** پیام‌های کالابرگ سداد (استعلام / خرید / لغو) — DE3 680000. */
@Singleton
class SadadCommodityBasketMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
) {
    suspend fun buildInquiry(request: SadadCommodityBasketInquiryRequest): IsoMessage =
        build(
            mti = SadadKeyConfig.INQUIRY_COMMODITY_BASKET_MTI,
            amount = request.totalAmount,
            track2 = request.track2,
            pinBlock = null,
            functionCode = FunctionCodeData(
                SadadCommodityBasketCodec.INQUIRY_FUNCTION_CODE,
                SadadCommodityBasketCodec.inquiryData(request.items),
            ),
        )

    suspend fun buildSale(request: SadadCommodityBasketSaleRequest): IsoMessage =
        build(
            mti = SadadKeyConfig.SALE_COMMODITY_BASKET_MTI,
            amount = request.transactionAmount,
            track2 = request.track2,
            pinBlock = request.pinBlock,
            functionCode = FunctionCodeData(
                SadadCommodityBasketCodec.SALE_FUNCTION_CODE,
                SadadCommodityBasketCodec.saleData(
                    request.transactionAmount,
                    request.creditAmount,
                    request.traceItem,
                ),
            ),
        )

    suspend fun buildCancel(request: SadadCommodityBasketCancelRequest): IsoMessage =
        build(
            mti = SadadKeyConfig.CANCEL_COMMODITY_BASKET_MTI,
            amount = null,
            track2 = request.track2,
            pinBlock = null,
            functionCode = FunctionCodeData(
                SadadCommodityBasketCodec.CANCEL_FUNCTION_CODE,
                SadadCommodityBasketCodec.cancelData(request.orderTraceId),
            ),
        )

    private suspend fun build(
        mti: String,
        amount: Long?,
        track2: String,
        pinBlock: String?,
        functionCode: FunctionCodeData,
    ): IsoMessage {
        messageSupport.beginSession()
        val message = messageProvider.create().apply {
            this.mti = mti
            processingCode = SadadKeyConfig.COMMODITY_BASKET_PROCESSING_CODE
            amount?.let { this.amount = messageSupport.formatIsoAmount(it.toString()) }
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            if (track2.isNotBlank()) this.track2 = messageSupport.normalizeTrack2(track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            if (!pinBlock.isNullOrBlank()) this.pinBlock = ISOUtil.hex2byte(pinBlock)
            transportData = messageSupport.initTransportData()
            privateUseField63 = Field63Generator.generate(listOf(functionCode))
        }
        message.requireSadad().unsetFields(60, 61)
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }
}
