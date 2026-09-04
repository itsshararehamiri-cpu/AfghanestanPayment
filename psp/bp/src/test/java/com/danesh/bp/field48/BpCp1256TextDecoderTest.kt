package com.danesh.bp.field48

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset

class BpCp1256TextDecoderTest {

    private val cp1256 = Charset.forName("cp1256")

    @Test
    fun decode_fixesMojibakeFromCp1256ReadAsLatin1() {
        val correct = "فروشگاه تست"
        val mojibake = String(correct.toByteArray(cp1256), Charsets.ISO_8859_1)

        assertEquals(correct, BpCp1256TextDecoder.decode(mojibake))
    }

    @Test
    fun decode_keepsAlreadyCorrectPersianText() {
        val correct = "فروشگاه تست"
        assertEquals(correct, BpCp1256TextDecoder.decode(correct))
    }

    @Test
    fun decode_keepsAsciiEnglishName() {
        assertEquals("Test Shop", BpCp1256TextDecoder.decode("Test Shop"))
    }

    @Test
    fun decode_blankReturnsBlank() {
        assertEquals("", BpCp1256TextDecoder.decode("   "))
    }
}
