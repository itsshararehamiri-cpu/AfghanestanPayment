package com.danesh.bp.init

import com.danesh.api.InitRequest
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.bp.field48.BpField48Composer
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.core.Device
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.requireBp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpInitMessageBuilder @Inject constructor(
    private val metadataProvider: PspDeviceMetadataProvider,
    private val rsaSession: BpInitRsaSession,
    private val macCalculator: BpMacCalculator,
    private val device: Device,
    private val contextProvider: TransactionContextProvider,
    private val messageProvider: IsoMessageProvider,
    private val field48Composer: BpField48Composer,
) {

    suspend fun build(request: InitRequest): IsoMessage {
        val serial = BpInitRsaSession.sanitizeToken(metadataProvider.deviceSerial())
        val ticket1 = BpInitRsaSession.sanitizeToken(request.firstBallotTicket)
        val ticket2 = BpInitRsaSession.sanitizeToken(request.secondBallotTicket)
        check(ticket1.isNotEmpty()) { "بلیط اول خالی است" }
        check(ticket2.isNotEmpty()) { "بلیط دوم خالی است" }
        val por = BpInitRsaSession.calculatePor(ticket1, serial)
        val field61 = BpInitRsaSession.formatField61(por)
        check(!field61.contains('@')) {
            "فیلد 61 Init باید فقط PoR باشد (بدون Serial@PoR)"
        }
        val sessionId = rsaSession.prepareKeyPair()
        val publicKeyDer = rsaSession.publicKeyDer(sessionId)
        val fingerprint = rsaSession.fingerprint(sessionId)
        BpInitRsaSession.validatePublicKeyDer(publicKeyDer)
        BpInitTrace.step(
            "MessageBuilder",
            "RSA PublicKey قبل از ارسال sessionId=$sessionId " +
                    "fingerprintSha256=$fingerprint derLen=${publicKeyDer.size}",
        )
        val dateTime = currentLocalDateTime()
        val message = messageProvider.create().apply {
            mti = TransactionIsoProfile.INIT.mti
            processingCode = TransactionIsoProfile.INIT.processingCode
            this.dateTime = dateTime
            stan = contextProvider.nextStan()
            messageReasonCode = BpKeyConfig.INIT_MESSAGE_REASON
            securityControlInfo = BpKeyConfig.NETWORK_FIELD53
            privateUseField61 = field61
            setPrivateUseField62Bytes(publicKeyDer)
            privateUseField63 = metadataProvider.metadata().toBpField63()
        }
        rsaSession.bindToStan(sessionId, message.stan)
        val sentKeyBytes = message.privateUseField62Bytes()
            ?: message.privateUseField62.toByteArray(Charsets.ISO_8859_1)
        check(sentKeyBytes.contentEquals(publicKeyDer)) {
            "Field62 ارسالی با DER تولیدی یکی نیست " +
                    "(sentLen=${sentKeyBytes.size} derLen=${publicKeyDer.size} " +
                    "fingerprint=$fingerprint)"
        }
        BpInitTrace.step(
            "MessageBuilder",
            "Field62 == DER OK stan=${message.stan} sessionId=$sessionId fingerprint=$fingerprint",
        )
        field48Composer.applyTerminalNetworkTags(message)
        message.requireBp().unsetFields(*TERMINAL_IDENTITY_FIELDS)
        macCalculator.applyInitMac(message)
        BpInitTrace.step("MessageBuilder", "پیام آماده ارسال")
        return message
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }

    companion object {
        private val TERMINAL_IDENTITY_FIELDS = intArrayOf(22, 24, 41, 42)
    }
}
