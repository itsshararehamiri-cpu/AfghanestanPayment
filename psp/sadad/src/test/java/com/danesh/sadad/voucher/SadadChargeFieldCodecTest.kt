package com.danesh.sadad.voucher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SadadChargeFieldCodecTest {

    @Test
    fun field48_leftPadsProviderCategoryAndCount() {
        assertEquals("091905 01", SadadChargeField48.format("919", "5", 1))
        assertEquals("093510 01", SadadChargeField48.format("935", "10", 1))
        assertEquals("001203 01", SadadChargeField48.format("12", "3", 1))
    }

    @Test
    fun field62_readsLiveCountedEncryptedPinBlock() {
        // Pin_Length=15 رقم، ولی داده یک بلوک ۸ بایتی رمزشده (۱۶ hex) است.
        val field = "011715511820004800620806A0A6B435C970071"
        val parsed = SadadChargeField62Parser.parse(field)
        assertEquals("51182000480062080", parsed?.serial)
        assertEquals("6A0A6B435C970071", parsed?.pin)
        assertEquals(true, parsed?.pinEncrypted)
        assertEquals(15, parsed?.pinLength)
        assertEquals(1, parsed?.pinCount)
    }

    @Test
    fun field62_encryptedPinHexLength() {
        assertEquals(16, SadadChargeField62Parser.encryptedPinHexLength(15))
        assertEquals(16, SadadChargeField62Parser.encryptedPinHexLength(16))
        assertEquals(16, SadadChargeField62Parser.encryptedPinHexLength(8))
        assertEquals(32, SadadChargeField62Parser.encryptedPinHexLength(17))
    }

    @Test
    fun field62_readsCountedSerialThenPin() {
        val parsed = SadadChargeField62Parser.parse("010808ABCD1234WXYZ9876")
        assertEquals("ABCD1234", parsed?.serial)
        assertEquals("WXYZ9876", parsed?.pin)
        assertEquals(1, parsed?.pinCount)
    }

    @Test
    fun field62_readsCountedPayloadFromIso8859Bytes() {
        val field = "011715511820004800620806A0A6B435C970071"
        val parsed = SadadChargeField62Parser.parse(field.toByteArray(Charsets.ISO_8859_1))
        assertEquals("51182000480062080", parsed?.serial)
        assertEquals("6A0A6B435C970071", parsed?.pin)
        assertEquals(true, parsed?.pinEncrypted)
    }

    @Test
    fun field62_rejectsShortCountedPayload() {
        assertNull(SadadChargeField62Parser.parse("0108"))
        assertNull(SadadChargeField62Parser.parse("010808ABCD"))
        assertNull(SadadChargeField62Parser.parse(""))
    }

    @Test
    fun field62_readsSerialPinAndUssdFromSpaceSeparatedAscii() {
        val parsed = SadadChargeField62Parser.parse("ABC123 456789 *141#")
        assertEquals("ABC123", parsed?.serial)
        assertEquals("456789", parsed?.pin)
        assertEquals("*141#", parsed?.ussd)
    }

    @Test
    fun field62_readsSpaceSeparatedPayloadFromHexDump() {
        val hex = "41424331323320343536373839202A31343123"
        val parsed = SadadChargeField62Parser.parse(hex)
        assertEquals("ABC123", parsed?.serial)
        assertEquals("456789", parsed?.pin)
        assertEquals("*141#", parsed?.ussd)
    }

    @Test
    fun field62_readsSerialPinAndUssdWhenZeroByteIsTheSeparator() {
        val hex = "313233343536373839300034383231002A3134302A23"
        val parsed = SadadChargeField62Parser.parse(hex)
        assertEquals("1234567890", parsed?.serial)
        assertEquals("4821", parsed?.pin)
        assertEquals("*140*#", parsed?.ussd)
    }

    @Test
    fun field62_readsAsciiWithNulSeparatorsFromCharPackager() {
        val field = "1234567890\u00004821\u0000*140*#"
        val parsed = SadadChargeField62Parser.parse(field)
        assertEquals("1234567890", parsed?.serial)
        assertEquals("4821", parsed?.pin)
        assertEquals("*140*#", parsed?.ussd)
    }
}
