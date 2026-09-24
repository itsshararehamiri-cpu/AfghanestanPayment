package com.danesh.sadad.topup

import com.danesh.api.ChargeCatalog
import com.danesh.api.ChargeKind
import com.danesh.api.TopUpUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadTopUpMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
    private val chargeCatalog: ChargeCatalog,
) {
    suspend fun build(request: TopUpUserInput): IsoMessage {
        messageSupport.beginSession()
        val product = chargeCatalog.findProduct(request.productId)
        val chargeDigits = request.amount.filter { it.isDigit() }
        val chargeAmount = chargeDigits.padStart(12, '0').takeLast(12)
        val taxPercent = chargeCatalog.operators(ChargeKind.TOPUP)
            .firstOrNull { it.providerId == product?.providerId }
            ?.taxPercent ?: 0
        val payable = payableWithTax(chargeDigits.toLongOrNull() ?: 0L, taxPercent)
        val operatorCode = (product?.providerId ?: request.operatorCode)
            .filter { it.isDigit() }.padStart(3, '0').takeLast(3)
        val categoryCode = (product?.categoryId ?: "1")
            .filter { it.isDigit() }.padStart(3, '0').takeLast(3)
        val serviceCode = (product?.serviceTypeCode ?: "1")
            .filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val mobile = request.mobileNumber.filter { it.isDigit() }
        val topupData = operatorCode + categoryCode + serviceCode + "0" + chargeAmount +
            mobile.length + mobile + mobile.length + mobile
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.TOPUP_MTI
            processingCode = SadadKeyConfig.TOPUP_PROCESSING_CODE
            stan = messageSupport.nextStan()
            amount = payable.toString().padStart(12, '0').takeLast(12)
            messageSupport.run { applySadadStandardTerminalFields() }
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            messageSupport.run { applySadadFunctionCode(SadadKeyConfig.SADAD_NII) }
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)

            privateUseField63 = Field63Generator.generate(
                listOf(FunctionCodeData(SadadKeyConfig.TOPUP_FUNCTION_CODE, topupData)),
            )
            transportData = messageSupport.initTransportData()
        }
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun payableWithTax(charge: Long, taxPercent: Int): Long {
        if (taxPercent <= 0) return charge
        return charge + charge * taxPercent / 100
    }
}
