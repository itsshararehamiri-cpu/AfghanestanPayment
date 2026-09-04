package com.danesh.bp.logon



import com.danesh.api.PspDeviceMetadataProvider

import com.danesh.api.TransactionContextProvider

import com.danesh.api.TransactionIsoProfile

import com.danesh.bp.field48.BpField48Composer

import com.danesh.bp.field63.toBpField63

import com.danesh.bp.key.BpKeyConfig

import com.danesh.bp.key.encodeHexKey

import com.danesh.bp.mac.BpMacCalculator

import com.danesh.iso.IsoMessage

import com.danesh.iso.IsoMessageProvider

import java.text.SimpleDateFormat

import java.util.Date

import java.util.Locale

import javax.inject.Inject

import javax.inject.Singleton



@Singleton

class BpLogonMessageBuilder @Inject constructor(

    private val metadataProvider: PspDeviceMetadataProvider,

    private val macCalculator: BpMacCalculator,

    private val contextProvider: TransactionContextProvider,

    private val messageProvider: IsoMessageProvider,

    private val field48Composer: BpField48Composer,

) {



    suspend fun build(): IsoMessage {

        val profile = TransactionIsoProfile.LOGON

        val transmissionTime = currentTransmissionDateTime()



        val message = messageProvider.create().apply {

            mti = profile.mti

            processingCode = profile.processingCode

            dateTime = transmissionTime

            stan = contextProvider.nextStan()

            messageReasonCode = BpKeyConfig.LOGON_MESSAGE_REASON

            securityControlInfo = BpKeyConfig.NETWORK_FIELD53

            privateUseField63 = metadataProvider.metadata().toBpField63()

        }

        field48Composer.applyTerminalNetworkTags(message)

        BpLogonTrace.step("MessageBuilder", "محاسبه MAC و inject فیلد 64")
        macCalculator.applyLogonMac(message)
        BpLogonTrace.step(
            "MessageBuilder",
            "F64 پس از inject=${message.mac?.encodeHexKey().orEmpty()}",
        )

        return message

    }




    private fun currentTransmissionDateTime(): String {

        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())

    }

}

