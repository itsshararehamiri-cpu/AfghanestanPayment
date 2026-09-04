package com.danesh.iso

import com.danesh.iso.field48.BpField48Tlv
import com.danesh.iso.packager.BpIso93BPackager
import org.jpos.iso.ISOMsg
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsoMessageMacTest {

    private val packager = BpIso93BPackager()

    @Test
    fun packForMac_setsBitmapBit64ButExcludesField64Bytes() {
        val isoMsg = buildInitLikeMessage()
        val message = wrap(isoMsg)

        val macInput = message.packForMac()
        val packedWithPlaceholder = wrap(buildInitLikeMessage()).let { clone ->
            clone.cloneForMacTesting().pack()
        }

        assertEquals(packedWithPlaceholder.size - BpIsoMessage.MAC_FIELD_LENGTH, macInput.size)
        assertArrayEquals(
            packedWithPlaceholder.copyOf(macInput.size),
            macInput,
        )
        // F61+F63+F64 → آخرین بایت bitmap = 0x0B (نه 0x0F چون F62 نیست)
        assertEquals(0x0B.toByte(), macInput[9])
        assertTrue(message.isPrimaryBitmapBit64Set(macInput))
        assertFalse(isoMsg.hasField(64))
    }

    @Test
    fun packForMac_balanceLikeMessage_setsBit64AndExcludesField64Bytes() {
        val bpPackager = this.packager
        val msg = ISOMsg().apply {
            setPackager(bpPackager)
            set(0, "0200")
            set(3, "310000")
            set(11, "000290")
            set(12, "260712223000")
            set(22, "021")
            set(25, "10")
            set(35, "5076772022603460=08095061770190600030")
            set(41, "06425048")
            set(48, "040011")
            set(49, "364")
            set(52, byteArrayOf(0x92.toByte(), 0xB4.toByte(), 0x68, 0xB2.toByte(), 0xF6.toByte(), 0x0A, 0x66, 0x29))
            set(53, "00001007")
            set(63, "D1V2890000001@1.0.0@869070069369828@")
        }
        val message = wrap(msg)
        val macInput = message.packForMac()

        assertTrue(message.isPrimaryBitmapBit64Set(macInput))
        val packedWithMac = wrap(msg).also { it.mac = ByteArray(8) }.packIsoBody()
        assertEquals(packedWithMac.size - 8, macInput.size)
        assertArrayEquals(packedWithMac.copyOf(macInput.size), macInput)
    }

    private fun wrap(isoMsg: ISOMsg): BpIsoMessage {
        val message = BpIsoMessage(BpField48Tlv())
        message.setPackager(packager)
        message.toIsoMessage(isoMsg)
        return message
    }

    private fun BpIsoMessage.cloneForMacTesting(): ISOMsg {
        val clone = getIsoMessage().clone() as ISOMsg
        if (clone.packager == null) {
            getIsoMessage().packager?.let { clone.setPackager(it) }
        }
        val macField = BpIsoMessage.resolveMacField(clone)
        clone.set(macField, ByteArray(BpIsoMessage.MAC_FIELD_LENGTH))
        return clone
    }

    private fun buildInitLikeMessage(): ISOMsg {
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(0, "0800")
        msg.set(3, "900000")
        msg.set(11, "122811")
        msg.set(12, "260531122811")
        msg.set(25, "10")
        msg.set(48, "13014192.168.100.8514009127.0.0.115004858501003PAX02003LAN03004S910")
        msg.set(53, "00001007")
        msg.set(61, "0CE6EEF5DA9E16DACD7E92A3E75D1EEE77D5A2918499056798F07AC0E6F773BC")
        msg.set(63, "MA2E2351269195@1.0.0")
        return msg
    }
}
