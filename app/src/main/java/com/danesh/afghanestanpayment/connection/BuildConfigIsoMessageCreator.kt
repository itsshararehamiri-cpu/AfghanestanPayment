package com.danesh.afghanestanpayment.connection

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.iso.BpIsoMessage
import com.danesh.iso.HpIsoMessage
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageCreator
import com.danesh.iso.SadadIsoMessage
import com.danesh.iso.field48.BpField48Tlv
import com.danesh.iso.field48.HpField48Tlv
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
        // فیلد ۴۸ سداد از تگ‌های ۳ کاراکتری استفاده می‌کند (مثل 018/019)، مطابق HpField48Tlv؛
        // BpField48Tlv تگ ۲ کاراکتری فرض می‌کند و آن‌ها را قطع می‌کرد.
        "SADAD" -> SadadIsoMessage().also { it.setPackager(packager) }
        else -> hpIsoMessageProvider.get().also { it.setPackager(packager) }
    }
}
