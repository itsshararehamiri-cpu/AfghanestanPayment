package com.danesh.afghanestanpayment.connection

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.iso.BpIsoMessage
import com.danesh.iso.HpIsoMessage
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageCreator
import com.danesh.iso.field48.BpField48Tlv
import org.jpos.iso.ISOPackager
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BuildConfigIsoMessageCreator @Inject constructor(
    private val hpIsoMessageProvider: Provider<HpIsoMessage>,
) : IsoMessageCreator {

    override fun create(packager: ISOPackager): IsoMessage = when (BuildConfig.ACTIVE_PSP) {
        "BP" -> BpIsoMessage(BpField48Tlv()).also { it.setPackager(packager) }
        else -> hpIsoMessageProvider.get().also { it.setPackager(packager) }
    }
}
