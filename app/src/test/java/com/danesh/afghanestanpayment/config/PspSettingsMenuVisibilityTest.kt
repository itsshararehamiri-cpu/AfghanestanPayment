package com.danesh.afghanestanpayment.config

import com.danesh.menu.model.MenuItemType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PspSettingsMenuVisibilityTest {

    @Test
    fun behpardakht_showsSupportServicesAndVatAndTms() {
        val visibility = providerFor(
            activePsp = ActivePsp.BP,
            enabledFeatures = setOf(
                MenuItemType.SUPPORT.name,
                MenuItemType.TOPUP.name,
            ),
        ).visibility()

        assertTrue(visibility.showSupportServices)
        assertTrue(visibility.showVatPercentage)
        assertTrue(visibility.showTmsSection)
        assertTrue(visibility.showMerchantShowFee)
        assertTrue(visibility.showMerchantMicroPaymentIndex)
        assertTrue(visibility.showSupportMicroPaymentIndex)
    }

    @Test
    fun hamrahPay_hidesSupportServicesAndVatAndTms() {
        val visibility = providerFor(
            activePsp = ActivePsp.HP,
            enabledFeatures = setOf(
                MenuItemType.PURCHASE.name,
                MenuItemType.BALANCE.name,
            ),
        ).visibility()

        assertFalse(visibility.showSupportServices)
        assertFalse(visibility.showVatPercentage)
        assertFalse(visibility.showTmsSection)
        assertFalse(visibility.showMerchantShowFee)
        assertFalse(visibility.showMerchantMicroPaymentIndex)
        assertFalse(visibility.showSupportMicroPaymentIndex)
    }

    @Test
    fun hamrahPay_withTopupEnabled_stillHidesVatPercentage() {
        val visibility = providerFor(
            activePsp = ActivePsp.HP,
            enabledFeatures = setOf(MenuItemType.TOPUP.name),
        ).visibility()

        assertFalse(visibility.showVatPercentage)
    }

    @Test
    fun behpardakht_withoutTopupEnabled_hidesVatPercentage() {
        val visibility = providerFor(
            activePsp = ActivePsp.BP,
            enabledFeatures = emptySet(),
        ).visibility()

        assertFalse(visibility.showVatPercentage)
    }

    private fun providerFor(
        activePsp: ActivePsp,
        enabledFeatures: Set<String>,
    ): PspSettingsMenuVisibility =
        PspSettingsMenuVisibility(
            AppRuntimeConfig(
                activePsp = activePsp,
                activeProtocol = ActiveProtocol.ISO,
                activeDevice = ActiveDevice.K9,
                enabledFeatures = enabledFeatures,
            ),
        )
}
