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

        return SettingsMenuVisibility(
            showSupportServices = isBehpardakht,
            showVatPercentage = MenuItemType.TOPUP.name in features,
            showTmsSection = isBehpardakht,
            showMerchantShowFee = isBehpardakht,
            showMerchantMicroPaymentIndex = isBehpardakht,
            showSupportMicroPaymentIndex = isBehpardakht,
        )
    }
}
