package com.danesh.afghanestanpayment.config

import com.danesh.menu.model.MenuItemType
import com.danesh.settings.config.SettingsMenuVisibility
import com.danesh.settings.config.SettingsMenuVisibilityProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PspSettingsMenuVisibility @Inject constructor(
    private val appRuntimeConfig: AppRuntimeConfig,
) : SettingsMenuVisibilityProvider {

    override fun visibility(): SettingsMenuVisibility {
        val features = appRuntimeConfig.enabledFeatures
        val isBehpardakht = appRuntimeConfig.activePsp.isBehpardakht
        val isSadad = appRuntimeConfig.activePsp.isSadad

        // کلیدگذاری/TMS در هر PSP روش متفاوتی دارد:
        // - به‌پرداخت: مقداردهی اولیه با دو بلیط کلید (dual-custodian) از منوی تنظیمات.
        // - سداد: LOGON مبتنی بر کارت کلید + فعال‌سازی ترمینال (Terminal Initializer) — نیازمند
        //   همان بخش تنظیمات پشتیبان/TMS با جریانی متفاوت از به‌پرداخت.
        // - همراه‌پی: ورود به شبکه (Sign-On) کاملاً خودکار در استارتاپ (Karen) است و به هیچ
        //   اقدام دستی‌ای در تنظیمات نیاز ندارد.
        val hasManualKeyManagement = isBehpardakht || isSadad

        return SettingsMenuVisibility(
            showSupportServices = hasManualKeyManagement,
            showVatPercentage = MenuItemType.TOPUP.name in features,
            showTmsSection = hasManualKeyManagement,
            showMerchantShowFee = isBehpardakht,
            showMerchantMicroPaymentIndex = isBehpardakht,
            showSupportMicroPaymentIndex = isBehpardakht,
        )
    }
}
