package com.danesh.iso.field48

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.Charset

class BpField48TlvTest {

    @Test
    fun unpack_parsesDocumentExample() {
        val tlv = BpField48Tlv()
        tlv.unpack(
            "13014192.168.100.8514014192.168.100.2015004858501003PAX02003LAN03004S910",
        )

        assertEquals("192.168.100.85", tlv.getNode("13"))
        assertEquals("192.168.100.20", tlv.getNode("14"))
        assertEquals("8585", tlv.getNode("15"))
        assertEquals("PAX", tlv.getNode("01"))
        assertEquals("LAN", tlv.getNode("02"))
        assertEquals("S910", tlv.getNode("03"))
    }

    @Test
    fun packText_includesPosTagsForK9() {
        val tlv = BpField48Tlv()
        tlv.addNode("13", "192.168.100.85")
        tlv.addNode("14", "127.0.0.1")
        tlv.addNode("15", "8585")
        tlv.addNode("01", "centerm")
        tlv.addNode("02", "LAN")
        tlv.addNode("03", "K9")

        val text = tlv.packText()
        assertEquals("centerm", tlv.getNode("01"))
        assertEquals("LAN", tlv.getNode("02"))
        assertEquals("K9", tlv.getNode("03"))
        assertTrue(text.endsWith("01007centerm02003LAN03002K9"))
    }

    @Test
    fun packText_matchesInitDumpExample() {
        val tlv = BpField48Tlv()
        tlv.addNode("13", "192.168.100.85")
        tlv.addNode("14", "127.0.0.1")
        tlv.addNode("15", "8585")
        tlv.addNode("01", "PAX")
        tlv.addNode("02", "LAN")
        tlv.addNode("03", "S910")

        val text = tlv.packText()
        assertEquals(
            "13014192.168.100.8514009127.0.0.115004858501003PAX02003LAN03004S910",
            text,
        )
        assertEquals(text, String(tlv.pack(), Charsets.US_ASCII))
    }

    @Test
    fun pack_roundTripsDocumentExample() {
        val tlv = BpField48Tlv()
        tlv.addNode("13", "192.168.100.85")
        tlv.addNode("14", "192.168.100.20")
        tlv.addNode("15", "8585")
        tlv.addNode("01", "PAX")
        tlv.addNode("02", "LAN")
        tlv.addNode("03", "S910")

        val packed = tlv.packText()
        assertEquals(
            "13014192.168.100.8514014192.168.100.2015004858501003PAX02003LAN03004S910",
            packed,
        )
    }

    @Test
    fun unpack_initDumpExample() {
        val tlv = BpField48Tlv()
        tlv.unpack(
            "13014192.168.100.8514009127.0.0.115004858501003PAX02003LAN03004S910",
        )

        assertEquals("192.168.100.85", tlv.getNode("13"))
        assertEquals("127.0.0.1", tlv.getNode("14"))
        assertEquals("8585", tlv.getNode("15"))
        assertEquals("PAX", tlv.getNode("01"))
        assertEquals("LAN", tlv.getNode("02"))
        assertEquals("S910", tlv.getNode("03"))
    }

    @Test
    fun pack_initDumpExample() {
        val tlv = BpField48Tlv()
        tlv.addNode("13", "192.168.100.85")
        tlv.addNode("14", "127.0.0.1")
        tlv.addNode("15", "8585")
        tlv.addNode("01", "PAX")
        tlv.addNode("02", "LAN")
        tlv.addNode("03", "S910")

        val packed = tlv.packText()
        assertEquals(
            "13014192.168.100.8514009127.0.0.115004858501003PAX02003LAN03004S910",
            packed,
        )
    }

    @Test
    fun lengthFieldIsThreeDigits() {
        val tlv = BpField48Tlv()
        tlv.addNode("04", "1")

        val packed = tlv.packText()
        assertEquals("04001", packed.take(5))
        assertEquals("1", packed.drop(5))
    }

    @Test
    fun pack_usesTag2Length3ValueFormat() {
        val tlv = BpField48Tlv()
        tlv.addNode("13", "212.16.73.141")
        tlv.addNode("15", "8585")

        val packed = tlv.packText()
        assertEquals(
            "13013212.16.73.141150048585",
            packed,
        )
        assertEquals(packed, String(tlv.pack(), Charsets.ISO_8859_1))
    }

    @Test
    fun unpack_persianMerchantNameByByteLength() {
        val cp1256 = Charset.forName("cp1256")
        val merchantName = "فروشگاه تست"
        val valueBytes = merchantName.toByteArray(cp1256)
        val tlvBytes = "09".toByteArray(Charsets.US_ASCII) +
            "%03d".format(valueBytes.size).toByteArray(Charsets.US_ASCII) +
            valueBytes

        val tlv = BpField48Tlv()
        tlv.unpack(tlvBytes)

        assertEquals(merchantName, tlv.getNode("09"))
    }

    @Test
    fun pack_convertsPersianDigitsInLength() {
        val tlv = BpField48Tlv()
        tlv.addNode("04", "1")

        val packed = tlv.pack()
        assertEquals("040011", String(packed, Charsets.US_ASCII))
    }
}
