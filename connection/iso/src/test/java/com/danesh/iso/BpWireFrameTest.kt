package com.danesh.iso

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class BpWireFrameTest {

    @Test
    fun parsePayloadAfterLength_splitsTpduAndIsoBody() {
        val tpdu = byteArrayOf(0x60, 0x01, 0x00, 0x00, 0x00)
        val isoBody = byteArrayOf(0x08, 0x10, 0x22, 0x30)
        val payload = tpdu + isoBody

        val parsed = BpWireFrame.parsePayloadAfterLength(payload)

        assertArrayEquals(tpdu, parsed.tpdu)
        assertArrayEquals(isoBody, parsed.isoBody)
        assertEquals(0x0810, ((parsed.isoBody[0].toInt() and 0xFF) shl 8) or (parsed.isoBody[1].toInt() and 0xFF))
    }

    @Test
    fun encodeLength_usesTwoBigEndianBytes() {
        assertArrayEquals(byteArrayOf(0x00, 0x7E), BpWireFrame.encodeLength(0x7E))
        assertArrayEquals(byteArrayOf(0x01, 0xED.toByte()), BpWireFrame.encodeLength(0x1ED))
    }

    @Test
    fun fullFrame_hasLengthThenSevenBytesBeforeIsoFields() {
        val tpdu = byteArrayOf(0x60, 0x01, 0x2D, 0x00, 0x00)
        val isoBody = hexToBytes("0810223000000201")
        val payload = tpdu + isoBody
        val lengthPrefix = BpWireFrame.encodeLength(payload.size)
        val fullFrame = lengthPrefix + payload

        assertEquals(7, BpWireFrame.LENGTH_BYTES + BpWireFrame.TPDU_BYTES)
        assertEquals(0x08, fullFrame[7].toInt() and 0xFF)
        assertEquals(0x10, fullFrame[8].toInt() and 0xFF)

        val parsed = BpWireFrame.parsePayloadAfterLength(
            fullFrame.copyOfRange(BpWireFrame.LENGTH_BYTES, fullFrame.size),
        )
        assertArrayEquals(tpdu, parsed.tpdu)
        assertEquals("0810", isoMti(parsed.isoBody))
    }

    @Test
    fun buildTpdu_isFiveBytesWithNii() {
        assertArrayEquals(
            byteArrayOf(0x60, 0x00, 0x01, 0x00, 0x00),
            BpWireFrame.buildTpdu("1"),
        )
        assertArrayEquals(
            byteArrayOf(0x60, 0x02, 0xBC.toByte(), 0x00, 0x00),
            BpWireFrame.buildTpdu("700"),
        )
    }

    @Test
    fun buildFrame_isLengthPlusHeaderPlusIso() {
        val tpdu = BpWireFrame.buildTpdu("1")
        val isoBody = hexToBytes("0800203000000001")
        val frame = BpWireFrame.buildFrame(tpdu, isoBody)

        assertEquals(2 + 5 + isoBody.size, frame.size)
        assertArrayEquals(BpWireFrame.encodeLength(5 + isoBody.size), frame.copyOfRange(0, 2))
        assertArrayEquals(tpdu, frame.copyOfRange(2, 7))
        assertArrayEquals(isoBody, frame.copyOfRange(7, frame.size))
    }

    private fun isoMti(isoBody: ByteArray): String =
        "%02X%02X".format(isoBody[0], isoBody[1])

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { index ->
            hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
}
