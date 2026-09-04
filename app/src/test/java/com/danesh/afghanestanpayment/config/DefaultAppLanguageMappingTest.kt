package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAppLanguageMappingTest {

    @Test
    fun behpardakht_defaultLanguage_isPersian() {
        assertEquals(AppLanguage.Other, ActivePsp.BP.toDefaultAppLanguage())
    }

    @Test
    fun hamrahPay_defaultLanguage_isDari() {
        assertEquals(AppLanguage.PersianDari, ActivePsp.HP.toDefaultAppLanguage())
        assertEquals(AppLanguage.PersianDari, ActivePsp.FANAVA.toDefaultAppLanguage())
        assertEquals(AppLanguage.PersianDari, ActivePsp.AP.toDefaultAppLanguage())
        assertEquals(AppLanguage.PersianDari, ActivePsp.PN.toDefaultAppLanguage())
    }
}
