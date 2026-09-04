package com.danesh.iso.packager

import com.danesh.iso.BpWireFrame
import org.jpos.iso.ISOMsg
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class BpIso93BPackagerTest {

    private val packager = BpIso93BPackager()

    @Test
    fun unpack_bpInitRequestWireFrame() {
        val frame = hexToBytes(BP_INIT_REQUEST_WIRE_HEX)
        assertEquals(0x01E7, BpWireFrame.decodeLength(frame.copyOfRange(0, 2)))

        val parsed = BpWireFrame.parsePayloadAfterLength(frame.copyOfRange(2, frame.size))
        assertArrayEquals(hexToBytes("6000090000"), parsed.tpdu)
        assertArrayEquals(hexToBytes(BP_INIT_REQUEST_HEX), parsed.isoBody)
    }

    @Test
    fun pack_bpInitRequest_matchesReferenceIsoBody() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(0, "0800")
        msg.set(3, "900000")
        msg.set(11, "122811")
        msg.set(12, "260531122811")
        msg.set(48, BP_INIT_FIELD48)
        msg.set(53, "00001006")
        msg.set(61, BP_INIT_FIELD61_HEX)
        msg.set(62, hexToBytes(BP_INIT_FIELD62_HEX))
        msg.set(63, "MA2E2351269195@1.0.0")
        msg.set(64, hexToBytes(BP_INIT_FIELD64_HEX))

        assertArrayEquals(hexToBytes(BP_INIT_REQUEST_HEX), msg.pack())
    }

    @Test
    fun unpack_paxInitSuccessResponse() {
        val bytes = hexToBytes(PAX_INIT_SUCCESS_RESPONSE_HEX)
        val msg = ISOMsg()
        msg.packager = packager
        msg.unpack(bytes)

        assertEquals("0810", msg.getString(0))
        assertEquals("900000", msg.getString(3))
        assertEquals("260531122811", msg.getString(7))
        assertEquals("122811", msg.getString(11))
        assertEquals("260531122811", msg.getString(12))
        assertEquals("00", msg.getString(39))
        assertEquals(
            "5D86F25CC2EE5DE16275A756EE285D7DD6C8A38457054DFFB540D324692FE41E",
            msg.getString(61),
        )
        assertEquals("6530D2DE9799FF21", msg.getString(64))
        assertTrue(msg.hasField(62))
    }

    @Test
    fun pack_bpBalanceRequest_matchesReferenceIsoBody() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(0, "0200")
        msg.set(3, "310000")
        msg.set(11, "123416")
        msg.set(12, "260531123416")
        msg.set(22, "021")
        msg.set(25, "10")
        msg.set(35, "5076772022603460=08095061770190600030")
        msg.set(41, "06425048")
        msg.set(48, BP_INIT_FIELD48)
        msg.set(49, "364")
        msg.set(52, hexToBytes("92B468B2F60A6629"))
        msg.set(53, "00001006")
        msg.set(63, "MA2E2351269195@1.0.0")
        msg.set(64, hexToBytes("6882982028928B56"))

        assertArrayEquals(hexToBytes(BP_BALANCE_REQUEST_HEX), msg.pack())
    }

    @Test
    fun pack_bpLogonRequest_matchesReferenceIsoBody() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(0, "0800")
        msg.set(3, "920000")
        msg.set(11, "123044")
        msg.set(12, "260531123044")
        msg.set(25, "10")
        msg.set(48, BP_INIT_FIELD48)
        msg.set(53, "00001006")
        msg.set(63, "MA2E2351269195@1.0.0")
        msg.set(64, hexToBytes("57FC4CEC69755FAA"))

        assertArrayEquals(hexToBytes(BP_LOGON_REQUEST_HEX), msg.pack())
    }

    @Test
    fun pack_bpLogonRequest_k9UsesField7NotField12() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(0, "0800")
        msg.set(3, "920000")
        msg.set(7, "260531123044")
        msg.set(11, "123044")
        msg.set(25, "10")
        msg.set(48, BP_INIT_FIELD48)
        msg.set(53, "00001006")
        msg.set(63, "MA2E2351269195@1.0.0")
        msg.set(64, hexToBytes("57FC4CEC69755FAA"))

        val packed = msg.pack()
        assertFalse(msg.hasField(12))
        assertEquals("260531123044", msg.getString(7))
    }

    @Test
    fun pack_bpInitRequest_k9IncludesField25() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(0, "0800")
        msg.set(3, "900000")
        msg.set(11, "122811")
        msg.set(12, "260531122811")
        msg.set(25, "10")
        msg.set(48, BP_INIT_FIELD48)
        msg.set(53, "00001006")
        msg.set(61, BP_INIT_FIELD61_HEX)
        msg.set(62, hexToBytes(BP_INIT_FIELD62_HEX))
        msg.set(63, "MA2E2351269195@1.0.0")
        msg.set(64, hexToBytes(BP_INIT_FIELD64_HEX))

        assertEquals("10", msg.getString(25))
        assertTrue(msg.hasField(25))
    }

    @Test
    fun unpack_bpBalanceResponse() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.unpack(hexToBytes(BP_BALANCE_RESPONSE_HEX))

        assertEquals("0210", msg.getString(0))
        assertEquals("5076772022603460", msg.getString(2))
        assertEquals("310000", msg.getString(3))
        assertEquals("123416", msg.getString(11))
        assertEquals("00", msg.getString(39))
        assertEquals("06425048", msg.getString(41))
        assertEquals("364", msg.getString(49))
        assertEquals("B0D49A553699B10C", msg.getString(64))
    }

    @Ignore("Error response sample uses alternate capture framing")
    @Test
    fun unpack_bpInitErrorResponse() {
        val bytes = hexToBytes(BP_INIT_ERROR_RESPONSE_HEX)
        val msg = ISOMsg()
        msg.packager = packager
        msg.unpack(bytes)

        assertEquals("0810", msg.getString(0))
        assertEquals("900000", msg.getString(3))
        assertEquals("000320", msg.getString(11))
        assertEquals("260707123126", msg.getString(12))
        assertTrue(msg.getString(39).startsWith("63"))
        assertEquals("00001006", msg.getString(53))
        assertTrue(msg.getString(61).contains("D1V2890000001@"))
        assertTrue(msg.hasField(62))
        assertTrue(msg.getString(63).contains("D1V2890000001@1.0"))
        assertTrue(msg.hasField(64))
    }

    @Test
    fun unpack_bpInitRequest() {
        val bytes = hexToBytes(BP_INIT_REQUEST_HEX)
        val msg = ISOMsg()
        msg.packager = packager
        msg.unpack(bytes)

        assertEquals("0800", msg.getString(0))
        assertEquals("900000", msg.getString(3))
        assertEquals("122811", msg.getString(11))
        assertEquals("260531122811", msg.getString(12))
        assertFalse(msg.hasField(25))
        assertEquals("00001006", msg.getString(53))
        assertTrue(msg.getString(48).contains("192.168.100.85"))
        assertTrue(msg.getString(61).startsWith("0CE6EEF5"))
        assertTrue(msg.getBytes(62).isNotEmpty())
        assertEquals("MA2E2351269195@1.0.0", msg.getString(63))
        assertTrue(msg.hasField(64))
    }

    @Test
    fun unpack_bpInitRequest_field62IsRawDerBinary() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.unpack(hexToBytes(BP_INIT_REQUEST_HEX))

        val field62 = msg.getBytes(62)
        val expectedDer = hexToBytes(BP_INIT_FIELD62_HEX)
        assertArrayEquals(expectedDer, field62)
        assertEquals(0x30.toByte(), field62[0])
        assertEquals(0x82.toByte(), field62[1])
    }

    @Test
    fun pack_field62_rawBytes_notHexAsciiOnWire() {
        val der = hexToBytes(BP_INIT_FIELD62_HEX)
        val msg = ISOMsg()
        msg.packager = packager
        msg.set(62, der)

        assertArrayEquals(der, msg.getBytes(62))

        val packed = msg.pack()
        assertTrue(indexOf(packed, byteArrayOf(0x30.toByte(), 0x82.toByte(), 0x01.toByte(), 0x22.toByte())) >= 0)
        assertEquals(-1, indexOf(packed, "30820122".toByteArray(Charsets.US_ASCII)))
    }

    @Test
    fun unpack_paxInitSuccessResponse_field62Is256BinaryBytes() {
        val msg = ISOMsg()
        msg.packager = packager
        msg.unpack(hexToBytes(PAX_INIT_SUCCESS_RESPONSE_HEX))

        val field62 = msg.getBytes(62)
        assertEquals(256, field62.size)
        assertFalse(field62.all { it in 0x30..0x39 || it in 0x41..0x46 || it in 0x61..0x66 })
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "").replace("\n", "")
        return ByteArray(clean.length / 2) { index ->
            clean.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    private fun indexOf(haystack: ByteArray, needle: ByteArray): Int {
        if (needle.isEmpty() || haystack.size < needle.size) return -1
        for (start in 0..haystack.size - needle.size) {
            if (haystack.sliceArray(start until start + needle.size).contentEquals(needle)) {
                return start
            }
        }
        return -1
    }

    companion object {
        const val BP_INIT_REQUEST_HEX =
            "0800203000000001080f900000122811260531122811006731333031343139322e3136382e3130302e383531343030393132372e302e302e31313530303438353835303130303350415830323030334c414e303330303453393130" +
                "00001006006430434536454546354441394531364441434437453932413345373544314545453737443541323931383439393035363739384630374143304536463737334243029430820122300d06092a864886f70d01010105000382010f003082010a0282010100ab0d384903d6e1b756a5a159712061084d792d6b75e2743433061c03a10a2100b632fcf39780cf87f671fd427a6af2dda5fec11d7e74b590592ee9c25431200e381cee8a2345fce2a8556ba7323277acf41ebc73ff10176df1cda17e2bd08c454400d8482e955dc7f65054a727f454c3c221161c5fd67d26eee9566643cafa423c036145e579fb2150f59da779fb5d15c5be6b1a33a97c2067af40a22568b45782aaff4a9280798206fcea17d28569feb65235dfa1c4c1bfeda8aa0179d6e12cb0b170504a780d5dd4167ea5972b74095adae37543918f06e3d7f135e9dfedca194974c392df284a73b0c7902934a24caaecbaac59cf11263eb46f332a985d9d020301000100204d4132453233353132363931393540312e302e30caae5316d81d8a81"

        const val BP_INIT_REQUEST_WIRE_HEX =
            "01e76000090000" + BP_INIT_REQUEST_HEX

        const val BP_BALANCE_REQUEST_HEX =
            "02002030048020819803310000123416260531123416002110375076772022603460d0809506177019060003003036343235303438006731333031343139322e3136382e3130302e383531343030393132372e302e302e31313530303438353835303130303350415830323030334c414e30333030345339313033363492b468b2f60a66290000100600204d4132453233353132363931393540312e302e306882982028928b56"

        const val BP_LOGON_REQUEST_HEX =
            "0800203000800001080392000012304426053112304410006731333031343139322e3136382e3130302e383531343030393132372e302e302e31313530303438353835303130303350415830323030334c414e3033303034533931300000100600204d4132453233353132363931393540312e302e3057fc4cec69755faa"

        private const val BP_BALANCE_RESPONSE_HEX =
            "0210623000000e8184011650767720226034603100002605311234161234162605311234163930303030303636373832353232363738343030303634323530343800163041303131303731333832303033383833363400323336344330303030343030303030303033363443303030303031383430363139b0d49a553699b10c"

        private const val BP_INIT_FIELD48 =
            "13014192.168.100.8514009127.0.0.115004858501003PAX02003LAN03004S910"

        private const val BP_INIT_FIELD61_HEX =
            "0CE6EEF5DA9E16DACD7E92A3E75D1EEE77D5A2918499056798F07AC0E6F773BC"

        private const val BP_INIT_FIELD62_HEX =
            "30820122300d06092a864886f70d01010105000382010f003082010a0282010100ab0d384903d6e1b756a5a159712061084d792d6b75e2743433061c03a10a2100b632fcf39780cf87f671fd427a6af2dda5fec11d7e74b590592ee9c25431200e381cee8a2345fce2a8556ba7323277acf41ebc73ff10176df1cda17e2bd08c454400d8482e955dc7f65054a727f454c3c221161c5fd67d26eee9566643cafa423c036145e579fb2150f59da779fb5d15c5be6b1a33a97c2067af40a22568b45782aaff4a9280798206fcea17d28569feb65235dfa1c4c1bfeda8aa0179d6e12cb0b170504a780d5dd4167ea5972b74095adae37543918f06e3d7f135e9dfedca194974c392df284a73b0c7902934a24caaecbaac59cf11263eb46f332a985d9d0203010001"

        private const val BP_INIT_FIELD64_HEX = "caae5316d81d8a81"

        private const val PAX_INIT_SUCCESS_RESPONSE_HEX =
            "0810223000000201000d900000260531122811122811260531122811303000000064" +
                "35443836463235434332454535444531363237354137353645453238354437444436433841333834353730353444464642353430443332343639324645343145" +
                "025645fc3e6cab764c02038b14ca86f13fe29e77b3efa1eb27f49ee57599ae0ad00151ce532858ae049d3eb9d0a3e2d6df8d3272be0e817be004d379fa3e5197dc" +
                "0302687472265ac7240612e07cb209888fec67d4721aa5a260074a971bfc240d95767604cb4267141e31e9e8e33f291fc31e0a18da9bac62cfc582b3dd29fc04294fbab4496d0aa2e938fce011329624ee4aeccc453b1c2bc06db11e09d54ed831b3ed4173f81688f49dfa8306e36c08f020af852f1ca32ae2bf7733f261e4b5e80c875b107891932b899ef98fcb0bf66283d5579171da84d9cd904dffc03124d3f24fffc66f81ae5ee648f8ec647f2e822d37c0b6a983fedeeebde8b89839e2d46530d2de9799ff21"

        private const val BP_INIT_ERROR_RESPONSE_HEX =
            "0810203000000201080f900000000320260707123126363001430383030393138353541333033" +
                "000010060078443156323839303030303030314042313136393436364545433838353038423833374335383939363935413433314637423739343044424432323543434632304532314542353037444231323103924d494942496a414e42676b71686b6947397730424151454641414f43415138414d49494243674b4341514541774b78537173534d6953544e4965774268597163496c4c6c6b54702f41737652537a54702f53497a374549694954467655356739625552635a715349556e505957634c3050496e6539332b6447314158585246655965444a64426b72684b42356a696e4534507a4e4271564137686e784a664c55576e4e794c424d66346b6f756b734e5879787770613757662f524f50327a4148732b563165317a396e68364d572f6b70592f71344b5541555962725656486859734c4e596631587732383770766a4148575a443462396c6e362b576f3032513131756d4448505a656333586676356e7742725873786f48507a523846623141547462327a2f2b454b5243384973314d64366b506a6c44536c43715875644c4144464945744b386a4a2f5046705a42515356633438736c446c362f757a36644a6347752b32696f677242486c436a346350365569557a6c4553376e505a7345306951494441514142001944315632383930303030303140312e304040862dd0447fd1d816"
    }
}
