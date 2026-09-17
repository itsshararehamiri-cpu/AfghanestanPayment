package com.danesh.bp.coupon

import com.danesh.api.CouponListUserInput
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpCouponListMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
) {

    suspend fun build(request: CouponListUserInput): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.COUPON_MTI
            processingCode = BpKeyConfig.COUPON_PROCESSING_CODE
            stan = BpKeyConfig.CASH_OUT_STAN
            dateTime = currentLocalDateTime()
            terminalId = config.terminalId
            setField48 {
                setField48Tag(
                    tag = BpKeyConfig.BALANCE_FIELD48_TAG,
                    value = lastSuccessValues.stanTagValue(),
                )
                setField48Tag(
                    tag = BpKeyConfig.COUPON_REQUEST_INDEX_TAG ,
                    value = request.requestedIndex.toString(),
                )
            }
            securityControlInfo = BpKeyConfig.NETWORK_FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
        }
        macCalculator.applyTransactionMac(message)
        return message
    }
//
//    private fun formatIsoAmount(amount: String): String {
//        val digits = amount.filter(Char::isDigit)
//        return digits.padStart(12, '0').takeLast(12)
//    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
