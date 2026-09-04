package com.danesh.bp.voucher

import android.util.Log
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TopUpUserInput
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.field22.BpPosEntryMode
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpTopUpMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
) {

    suspend fun build(request: TopUpUserInput): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val chargeAmount = BpTopUpAmountCalculator.parseChargeAmount(request.amount)
        val totalAmount = if(request.operatorCode=="2")BpTopUpAmountCalculator.totalWithVat(
            chargeAmount = chargeAmount,
            vatPercentRaw = contextProvider.getVatPercentage(),
        )else chargeAmount
        val chargeAmountIso = BpTopUpAmountCalculator.formatIsoAmount(chargeAmount)
        val totalAmountIso = BpTopUpAmountCalculator.formatIsoAmount(totalAmount)
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.TOPUP_MTI
            processingCode = BpKeyConfig.TOPUP_PROCESSING_CODE
            amount = totalAmountIso
            stan = contextProvider.nextStan()
            dateTime = currentLocalDateTime()
            pointOfServiceEntryMode = BpPosEntryMode.forCurrentCardRead()
            messageReasonCode = BpKeyConfig.VOUCHER_MESSAGE_REASON
            track2 = request.track2
            terminalId = config.terminalId
            currency = BpKeyConfig.VOUCHER_CURRENCY
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {

                setField48Tag(
                    tag = BpKeyConfig.VOUCHER_FIELD48_TAG_AMOUNT,
                    value = chargeAmountIso,
                )
                setField48Tag(
                    tag = BpKeyConfig.VOUCHER_FIELD48_TAG_OPERATOR,
                    value = request.operatorCode,
                )
                setField48Tag(
                    tag = BpKeyConfig.VOUCHER_FIELD48_TAG_MOBILE,
                    value = request.mobileNumber,
                )

                setField48Tag(
                    tag = BpKeyConfig.TOP_UP_FIELD48_TAG_SERVICE_CODE,
                    value = "1",
                )
                setField48 {
                    setField48Tag(
                        tag = BpKeyConfig.ADVICE_FIELD48_TAG,
                        value = lastSuccessValues.stanTagValue(),
                    )
                }
            }
        }
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
