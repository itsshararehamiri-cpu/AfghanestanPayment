package com.danesh.bp.support

import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.SupportUserInput
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
class BpSupportMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
) {

    suspend fun build(request: SupportUserInput): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.SUPPORT_MTI
            processingCode = BpKeyConfig.SUPPORT_PROCESSING_CODE
            amount = formatIsoAmount(request.amount)
            stan = contextProvider.nextStan()
            dateTime = currentLocalDateTime()
            pointOfServiceEntryMode = BpPosEntryMode.forCurrentCardRead()
            messageReasonCode = BpKeyConfig.SUPPORT_MESSAGE_REASON
            track2 = request.track2
            terminalId = config.terminalId
            currency = BpKeyConfig.SUPPORT_CURRENCY
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {
                // تگ 04 یا 05 الزامی
                setField48Tag(
                    tag = BpKeyConfig.SUPPORT_FIELD48_TAG_STAN,
                    value = lastSuccessValues.stanTagValue(),
                )
                // تگ 24 الزامی — شناسه آیتم پشتیبانی
                setField48Tag(
                    tag = BpKeyConfig.SUPPORT_FIELD48_TAG_SERVICE,
                    value = request.serviceId,
                )
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
