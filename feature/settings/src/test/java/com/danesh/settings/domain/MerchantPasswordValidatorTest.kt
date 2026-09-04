package com.danesh.settings.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantPasswordValidatorTest {

    @Test
    fun rejectsSameDigitPasswords() {
        assertFalse(MerchantPasswordValidator.isSecure("1111"))
        assertFalse(MerchantPasswordValidator.isSecure("0000"))
    }

    @Test
    fun rejectsBlockedSequences() {
        assertFalse(MerchantPasswordValidator.isSecure("1234"))
        assertFalse(MerchantPasswordValidator.isSecure("4321"))
    }

    @Test
    fun acceptsSecurePasswords() {
        assertTrue(MerchantPasswordValidator.isSecure("2580"))
        assertTrue(MerchantPasswordValidator.isSecure("9071"))
    }
}
