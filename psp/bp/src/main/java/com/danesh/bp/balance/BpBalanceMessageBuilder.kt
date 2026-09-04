package com.danesh.bp.balance

import com.danesh.api.BalanceUserInput
import com.danesh.api.PspDeviceMetadataProvider
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
class BpBalanceMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
) {

    suspend fun build(request: BalanceUserInput): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.BALANCE_MTI
            processingCode = BpKeyConfig.BALANCE_PROCESSING_CODE
            stan = contextProvider.nextStan()
            dateTime = currentLocalDateTime()
            pointOfServiceEntryMode = BpPosEntryMode.forCurrentCardRead()
            messageReasonCode = BpKeyConfig.BALANCE_MESSAGE_REASON
            track2 = request.track2
            terminalId = config.terminalId
            currency = BpKeyConfig.BALANCE_CURRENCY
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {
                setField48Tag(
                    tag = BpKeyConfig.BALANCE_FIELD48_TAG,
                    value = lastSuccessValues.stanTagValue(),
                )
            }
        }
        BpBalanceTrace.verifyMacBitmap(message)
        macCalculator.applyTransactionMac(message)
        BpBalanceTrace.step("MAC استعلام موجودی با کلید کاری PED تولید شد")
        return message
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
