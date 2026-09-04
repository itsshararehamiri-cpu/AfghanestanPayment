package com.danesh.sadad.name_inquiry

import com.danesh.api.NameInquiryRequest
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadNameInquiryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: NameInquiryRequest): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.NAME_INQUIRY
        val destination = request.destination.filter { it.isDigit() }
        return messageProvider.create().apply {
            messageSupport.run {
                applySadadCardFields(
                    profile = profile,
                    session = session,
                    pan = when {
                        request.forWalletToWallet -> request.sourceWallet.filter { it.isDigit() }.take(16)
                        request.forWallet -> request.pan
                        else -> destination.take(16)
                    },
                    amount = "0",
                    track2 = request.track2,
                    pinBlock = "",
                    includeTrack2 = false,
                )
            }
            setRrn(session.dateTime)
            when {
                request.forWalletToWallet -> {
                    setField48 {
                        setTransactionType(FUNCTION_CODE)
                        setCard2NNumber(destination.take(16))
                    }
                }
                request.forWallet -> {
                    setField48 {
                        setTransactionType(FUNCTION_CODE)
                        setField48Tag(SadadKeyConfig.WALLET_TAG, destination.take(8))
                    }
                }
            }
        }
    }

    companion object {
        private const val FUNCTION_CODE = "651"
    }
}
