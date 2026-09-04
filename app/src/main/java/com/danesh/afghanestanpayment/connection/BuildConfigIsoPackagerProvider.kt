package com.danesh.afghanestanpayment.connection

import android.util.Log
import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.iso.IsoPackagerProvider
import com.danesh.iso.packager.BpIso93BPackager
import com.danesh.iso.packager.HpIso93BPackager
import org.jpos.iso.ISOPackager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigIsoPackagerProvider @Inject constructor() : IsoPackagerProvider {

    override fun create(): ISOPackager = when (BuildConfig.ACTIVE_PSP) {
        "BP" -> {
            BpIso93BPackager()
        }
        else ->{
            HpIso93BPackager()
        }
    }
}
