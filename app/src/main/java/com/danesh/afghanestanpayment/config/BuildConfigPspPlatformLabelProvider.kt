package com.danesh.afghanestanpayment.config

import com.danesh.common.psp.PspPlatformLabelProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigPspPlatformLabelProvider @Inject constructor(
    private val appRuntimeConfig: AppRuntimeConfig,
) : PspPlatformLabelProvider {

    override fun displayName(): String = when (appRuntimeConfig.activePsp) {
        ActivePsp.BP -> "به‌پرداخت"
        ActivePsp.HP -> "همراه‌پی"
        ActivePsp.FANAVA -> "فناوا"
        ActivePsp.AP -> "آسیاپی"
        ActivePsp.PN -> "پویان"
        ActivePsp.SADAD -> "سداد"
    }
}
