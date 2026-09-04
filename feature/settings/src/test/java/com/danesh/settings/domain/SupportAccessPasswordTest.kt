package com.danesh.settings.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class SupportAccessPasswordTest {

    @Test
    fun expectedPassword_example1524() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 24)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals("4251", SupportAccessPassword.expectedPassword(calendar))
    }

    @Test
    fun expectedPassword_reversesHourAndMinute() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 26)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals("6251", SupportAccessPassword.expectedPassword(calendar))
    }

    @Test
    fun expectedPassword_padsSingleDigitHourAndMinute() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals("5090", SupportAccessPassword.expectedPassword(calendar))
    }

    @Test
    fun matches_acceptsReversedCurrentTimeOnly() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 26)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertTrue(SupportAccessPassword.matches("6230", calendar))
        assertFalse(SupportAccessPassword.matches("0326", calendar))
        assertFalse(SupportAccessPassword.matches("2222", calendar))
    }
}
