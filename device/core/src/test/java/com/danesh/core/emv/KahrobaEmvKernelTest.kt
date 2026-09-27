package com.danesh.core.emv

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.SecureRandom
import java.util.Calendar

class KahrobaEmvKernelTest {

    private val track2Hex = "6037991234567890D25122010000F"
    private val panHex = "6037991234567890"
    private val cdol1Hex = "9F02069F03069F1A0295055F2A029A039C019F37049F4E05"

    /** کارت شبیه‌سازی‌شده؛ هر فرمان ثبت می‌شود. */
    private class FakeCard(private val responses: (ByteArray) -> String) {
        val commands = mutableListOf<ByteArray>()
        fun transceive(apdu: ByteArray): ByteArray {
            commands += apdu
            return responses(apdu).hexToBytes()
        }
    }

    private fun tlv(tag: String, value: String): String {
        val len = value.length / 2
        return tag + "%02X".format(len) + value
    }

    private fun card(gpoC5: String? = "85", genAcFormat1: Boolean = false) = FakeCard { apdu ->
        val ins = apdu[1].toInt() and 0xFF
        when (ins) {
            0xA4 -> "6F00" + "9000"
            0xA8 -> {
                val body = tlv("82", "1980") + tlv("94", "08010100") +
                    (gpoC5?.let { tlv("C5", it) } ?: "")
                tlv("77", body) + "9000"
            }
            0xB2 -> {
                val record = tlv("57", track2Hex.let { if (it.length % 2 == 1) it + "F" else it }) +
                    tlv("5A", panHex) + tlv("8C", cdol1Hex) + tlv("9F61", "00001234")
                tlv("70", record) + "9000"
            }
            0xAE -> if (genAcFormat1) {
                tlv("80", "80" + "0001" + "1122334455667788" + "06010A03A00000") + "9000"
            } else {
                tlv(
                    "77",
                    tlv("9F27", "80") + tlv("9F36", "0001") +
                        tlv("9F26", "1122334455667788") + tlv("9F10", "06010A03A00000"),
                ) + "9000"
            }
            else -> "6D00"
        }
    }

    private val fixedClock: () -> Calendar = {
        Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 27, 14, 5, 9) }
    }

    private fun kernel(fake: FakeCard) = KahrobaEmvKernel(
        transceive = fake::transceive,
        clock = fixedClock,
        random = SecureRandom(),
    )

    @Test
    fun `purchase flow produces track2 pan and field 55`() {
        val fake = card()
        val result = kernel(fake).run(
            KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "150000", "SHOP"),
        )

        assertEquals("6037991234567890=25122010000", result.track2)
        assertEquals("6037991234567890", result.pan)
        assertFalse(result.cardRequestsPin)
        assertEquals("1234", result.kahrobaToken)
        assertEquals("A0000008030001", result.aid)

        val f55 = TlvParser.parse(result.iccData.hexToBytes())
        assertEquals("0978", TlvParser.find(f55, 0x5F2A)!!.toHex())
        assertEquals("1980", TlvParser.find(f55, 0x82)!!.toHex())
        assertEquals("000000150000", TlvParser.find(f55, 0x9F02)!!.toHex())
        assertEquals("260927", TlvParser.find(f55, 0x9A)!!.toHex())
        assertEquals("31", TlvParser.find(f55, 0x9C)!!.toHex())
        assertEquals("1122334455667788", TlvParser.find(f55, 0x9F26)!!.toHex())
        assertEquals("80", TlvParser.find(f55, 0x9F27)!!.toHex())
        assertEquals("0001", TlvParser.find(f55, 0x9F36)!!.toHex())
        assertEquals("06010A03A00000", TlvParser.find(f55, 0x9F10)!!.toHex())
        assertEquals("A0000008030001", TlvParser.find(f55, 0x84)!!.toHex())
        assertEquals(panHex, TlvParser.find(f55, 0x5A)!!.toHex())

        // ترتیب فرمان‌ها: PPSE، AID، GPO، READ RECORD، GENERATE AC
        assertEquals(listOf(0xA4, 0xA4, 0xA8, 0xB2, 0xAE), fake.commands.map { it[1].toInt() and 0xFF })
        assertArrayEquals(
            "00A404000E325041592E5359532E444446303100".hexToBytes(),
            fake.commands[0],
        )
        assertArrayEquals("00A4040007A000000803000100".hexToBytes(), fake.commands[1])
        // READ RECORD: SFI 1, record 1 → P2 = 0C
        assertArrayEquals("00B2010C00".hexToBytes(), fake.commands[3])
    }

    @Test
    fun `gpo command carries amount and transaction type like kahroba c`() {
        val fake = card()
        kernel(fake).run(KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "150000"))
        val gpo = fake.commands[2].toHex()
        assertEquals(
            "80A8000013" + "8311" + "3400400288" + "000000150000" + "0978" + "0364" + "21" + "31" + "00",
            gpo,
        )
    }

    @Test
    fun `generate ac data follows cdol1`() {
        val fake = card()
        val result = kernel(fake).run(
            KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "150000", "SHOP"),
        )
        val genAc = fake.commands[4]
        val un = TlvParser.find(result.iccData.hexToBytes(), 0x9F37)!!.toHex()
        val expectedData = "000000150000" + "000000000000" + "0364" + "8000000000" + "0978" +
            "260927" + "31" + un + "53484F5020"
        assertEquals("80AE8000" + "%02X".format(expectedData.length / 2) + expectedData + "00", genAc.toHex())
    }

    @Test
    fun `balance uses type 00 and zero amount`() {
        val fake = card()
        val result = kernel(fake).run(KahrobaEmvParams(KahrobaTransactionType.BALANCE, ""))
        val f55 = TlvParser.parse(result.iccData.hexToBytes())
        assertEquals("00", TlvParser.find(f55, 0x9C)!!.toHex())
        assertEquals("000000000000", TlvParser.find(f55, 0x9F02)!!.toHex())
    }

    @Test
    fun `card without C5 85 requests pin`() {
        assertTrue(kernel(card(gpoC5 = "05")).run(KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "1000")).cardRequestsPin)
        assertTrue(kernel(card(gpoC5 = null)).run(KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "1000")).cardRequestsPin)
    }

    @Test
    fun `format 1 generate ac response is decoded`() {
        val result = kernel(card(genAcFormat1 = true))
            .run(KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "1000"))
        val f55 = TlvParser.parse(result.iccData.hexToBytes())
        assertEquals("1122334455667788", TlvParser.find(f55, 0x9F26)!!.toHex())
        assertEquals("0001", TlvParser.find(f55, 0x9F36)!!.toHex())
    }

    @Test
    fun `select failure aborts flow`() {
        val fake = FakeCard { "6A82" }
        try {
            kernel(fake).run(KahrobaEmvParams(KahrobaTransactionType.PURCHASE, "1000"))
            fail("expected exception")
        } catch (e: KahrobaEmvException) {
            assertTrue(e.message!!.contains("6A82"))
        }
    }

    @Test
    fun `amount to bcd`() {
        assertEquals("000000150000", KahrobaEmvKernel.amountToBcd("150,000").toHex())
        assertEquals("000000000000", KahrobaEmvKernel.amountToBcd("").toHex())
        assertEquals("000000150000", KahrobaEmvKernel.amountToBcd("۱۵۰۰۰۰").toHex())
    }

    @Test
    fun `tlv builder round trips`() {
        val bytes = TlvBuilder().add(0x9F02, "000000001000".hexToBytes()).add(0x57, ByteArray(0)).build()
        assertEquals("9F0206000000001000", bytes.toHex())
    }
}
