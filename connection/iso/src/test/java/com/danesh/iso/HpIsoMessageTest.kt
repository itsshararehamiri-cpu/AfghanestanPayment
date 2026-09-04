package com.danesh.iso

import com.danesh.iso.field48.HpField48Tlv
import org.junit.Assert.assertEquals
import org.junit.Test

class HpIsoMessageTest {

    @Test
    fun setField48_packsThreeCharTagTlv() {
        val message = HpIsoMessage()
        message.setField48 {
            setSerial("ABC123")
            setTransactionType("618")
        }

        val field48Text = String(message.getIsoMessage().getBytes(48), Charsets.UTF_8)
        assertEquals("001006ABC123002003618", field48Text)

        val tlv = HpField48Tlv()
        tlv.unpack(field48Text)
        assertEquals("ABC123", tlv.getNode("001"))
        assertEquals("618", tlv.getNode("002"))
    }
}
