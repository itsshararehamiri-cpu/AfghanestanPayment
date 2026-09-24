package com.danesh.common.currency

import com.danesh.common.locale.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountInWordsTest {

    @Test
    fun persianWords() {
        assertEquals("بیست و یک", AmountInWords.convert("21", AppLocale.IRANIAN))
        assertEquals("صد و پنج", AmountInWords.convert("105", AppLocale.IRANIAN))
        assertEquals("یک هزار و دویست", AmountInWords.convert("1200", AppLocale.IRANIAN))
        assertEquals("دوازده هزار و پانصد", AmountInWords.convert("12,500", AppLocale.DARI))
        assertEquals("دو میلیون و پانصد هزار", AmountInWords.convert("2500000", AppLocale.IRANIAN))
        assertEquals("یک میلیون و یک هزار و یک", AmountInWords.convert("1001001", null))
        assertEquals("یک هزار و دویست و سی و چهار", AmountInWords.convert("۱۲۳۴", AppLocale.IRANIAN))
    }

    @Test
    fun englishWords() {
        assertEquals("twenty-one", AmountInWords.convert("21", AppLocale.ENGLISH))
        assertEquals("one thousand two hundred", AmountInWords.convert("1200", AppLocale.ENGLISH))
        assertEquals(
            "two million five hundred thousand",
            AmountInWords.convert("2500000", AppLocale.ENGLISH),
        )
    }

    @Test
    fun zeroAndInvalid() {
        assertEquals("صفر", AmountInWords.convert("000", AppLocale.IRANIAN))
        assertEquals("zero", AmountInWords.convert("0", AppLocale.ENGLISH))
        assertNull(AmountInWords.convert("", AppLocale.IRANIAN))
        assertNull(AmountInWords.convert("12a", AppLocale.IRANIAN))
        assertNull(AmountInWords.convert("1234567890123456", AppLocale.IRANIAN))
    }
}
