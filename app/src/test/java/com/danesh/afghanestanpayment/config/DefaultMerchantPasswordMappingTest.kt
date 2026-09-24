package com.danesh.afghanestanpayment.config

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultMerchantPasswordMappingTest {

    @Test
    fun sadad_defaultMerchantPassword_is0000() {
        assertEquals("0000", ActivePsp.SADAD.toDefaultMerchantPassword())
    }

    @Test
    fun hamrahPay_defaultMerchantPassword_is1111() {
        assertEquals("1111", ActivePsp.HP.toDefaultMerchantPassword())
    }

    @Test
    fun behpardakht_defaultMerchantPassword_is1111() {
        assertEquals("1111", ActivePsp.BP.toDefaultMerchantPassword())
    }
}
