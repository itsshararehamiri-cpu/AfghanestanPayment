package com.danesh.common.locale

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantNameDisplayTest {

    @Test
    fun englishLanguageUsesEnglishName() {
        assertEquals(
            "Test Shop",
            MerchantNameDisplay.resolve("فروشگاه", "Test Shop", AppLanguage.English),
        )
    }

    @Test
    fun persianLanguageUsesPersianName() {
        assertEquals(
            "فروشگاه",
            MerchantNameDisplay.resolve("فروشگاه", "Test Shop", AppLanguage.Persian),
        )
    }

    @Test
    fun fallsBackWhenPreferredNameMissing() {
        assertEquals(
            "فروشگاه",
            MerchantNameDisplay.resolve("فروشگاه", "", AppLanguage.English),
        )
    }
}
