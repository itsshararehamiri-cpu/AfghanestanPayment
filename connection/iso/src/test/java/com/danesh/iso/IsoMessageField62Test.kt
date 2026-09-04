package com.danesh.iso

import com.danesh.iso.field48.BpField48Tlv
import com.danesh.iso.packager.BpIso93BPackager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class IsoMessageField62Test {

    private val packager = BpIso93BPackager()

    @Test
    fun setPrivateUseField62Bytes_roundTripsRawBinaryWithBpPackager() {
        val der = hexToBytes(BP_INIT_FIELD62_HEX)
        val message = BpIsoMessage(BpField48Tlv()).apply {
            setPackager(packager)
            setPrivateUseField62Bytes(der)
        }

        assertArrayEquals(der, message.privateUseField62Bytes())
        assertEquals(der.size, message.getIsoMessage().getBytes(62).size)
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "").replace("\n", "")
        return ByteArray(clean.length / 2) { index ->
            clean.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    companion object {
        private const val BP_INIT_FIELD62_HEX =
            "30820122300d06092a864886f70d01010105000382010f003082010a0282010100ab0d384903d6e1b756a5a159712061084d792d6b75e2743433061c03a10a2100b632fcf39780cf87f671fd427a6af2dda5fec11d7e74b590592ee9c25431200e381cee8a2345fce2a8556ba7323277acf41ebc73ff10176df1cda17e2bd08c454400d8482e955dc7f65054a727f454c3c221161c5fd67d26eee9566643cafa423c036145e579fb2150f59da779fb5d15c5be6b1a33a97c2067af40a22568b45782aaff4a9280798206fcea17d28569feb65235dfa1c4c1bfeda8aa0179d6e12cb0b170504a780d5dd4167ea5972b74095adae37543918f06e3d7f135e9dfedca194974c392df284a73b0c7902934a24caaecbaac59cf11263eb46f332a985d9d0203010001"
    }
}
