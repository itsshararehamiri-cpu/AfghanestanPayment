package com.danesh.bp.voucher

import android.util.Log
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionContextProvider
import com.danesh.api.VoucherUserInput
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.field22.BpPosEntryMode
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.bp.key.BpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpVoucherMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
) {

    suspend fun build(request: VoucherUserInput): IsoMessage {
        try {
            val config = try {
                contextProvider.getTerminalConfig()
            }
            catch (e: Exception){
                TerminalConfig("","","","","","","","",)
            }
            val message = messageProvider.create().apply {
                mti = BpKeyConfig.VOUCHER_MTI
                processingCode = BpKeyConfig.VOUCHER_PROCESSING_CODE
                amount = formatIsoAmount(request.amount)
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
                        tag = BpKeyConfig.VOUCHER_FIELD48_TAG_OPERATOR,
                        value = request.operatorCode,
                    )
                    setField48Tag(
                        tag = BpKeyConfig.VOUCHER_FIELD48_TAG_AMOUNT,
                        value = formatIsoAmount(request.amount),
                    )
                    setField48Tag(
                        tag = BpKeyConfig.VOUCHER_FIELD48_TAG,
                        value = lastSuccessValues.stanTagValue(),
                    )
                }
            }
            macCalculator.applyTransactionMac(message)
            return message
        }
        catch (e: Exception){
            return  messageProvider.create()
        }
    }

    private fun formatIsoAmount(amount: String): String {
        val digits = amount.filter(Char::isDigit)
        return digits.padStart(12, '0').takeLast(12)
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
