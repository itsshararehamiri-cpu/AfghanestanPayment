package com.danesh.bp.purchase

import com.danesh.api.PurchaseUserInput
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.field22.BpPosEntryMode
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.field48.BpField48Tags
import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.common.connection.DefaultDepositIdProvider
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpPurchaseMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
    private val defaultDepositIdProvider: DefaultDepositIdProvider,
) {

    suspend fun build(request: PurchaseUserInput): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.PURCHASE_MTI



            processingCode = BpKeyConfig.PURCHASE_PROCESSING_CODE
            amount = formatIsoAmount(request.amount)
            stan = contextProvider.nextStan()
            dateTime = currentLocalDateTime()
            pointOfServiceEntryMode = BpPosEntryMode.forCurrentCardRead()
            messageReasonCode = BpKeyConfig.PURCHASE_MESSAGE_REASON
            track2 = request.track2
            terminalId = config.terminalId
            currency = BpKeyConfig.PURCHASE_CURRENCY
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {
                setField48Tag(
                    tag = BpKeyConfig.PURCHASE_FIELD48_TAG,
                    value = lastSuccessValues.stanTagValue(),
                )
                defaultDepositIdProvider.activeDepositId()?.let { depositId ->
                    setField48Tag(
                        tag = BpField48Tags.PAYMENT_ID,
                        value = depositId,
                    )
                }
            }
        }
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun formatIsoAmount(amount: String): String {
        val digits = amount.filter(Char::isDigit)
        return digits.padStart(12, '0').takeLast(12)
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
