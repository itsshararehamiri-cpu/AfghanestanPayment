package com.danesh.sadad.init

import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.Field63Parser
import com.danesh.sadad.util.FunctionCodeData
import com.danesh.sadad.util.IranSystemEncoding
import org.junit.Assert.assertEquals
import org.junit.Test

class SadadField63CodecTest {

    @Test
    fun connectionFunctionCode_matchesDocumented01040000() {
        val packed = Field63Generator.generate(
            listOf(FunctionCodeData(SadadKeyConfig.FUNCTION_CODE_CONNECTION, "")),
        )
        assertEquals("01040000", packed)
        val parsed = Field63Parser.parse(packed)
        assertEquals(1, parsed.size)
        assertEquals("040", parsed[0].code)
        assertEquals("", parsed[0].data)
    }

    @Test
    fun inquiryBnplSample_countCodeLenData() {
        val packed = "0104800201"
        val parsed = Field63Parser.parse(packed)
        assertEquals(1, parsed.size)
        assertEquals("048", parsed[0].code)
        assertEquals("01", parsed[0].data)
        assertEquals(packed, Field63Generator.generate(parsed))
    }

    @Test
    fun terminalInitializer_parsesSadadInitSample() {
        val field63 = "01013076" +
            "34091903" +
            "000000131678530" +
            "006" + "96A897" +
            "005" + "sadad" +
            "000" +
            "000" +
            "012" + "021-22598009" +
            "0000111111" +
            "00" + "00" + "0"
        assertEquals(8 + 76, field63.length)

        val blocks = Field63Parser.parse(field63)
        assertEquals("013", blocks.single().code)
        assertEquals(76, blocks.single().data.length)

        val parsed = SadadTerminalInitializerCodec.parse(blocks.single().data)!!
        assertEquals("34091903", parsed.terminalId)
        assertEquals("000000131678530", parsed.acqId)
        assertEquals("تست", parsed.acqNameFa)
        assertEquals("sadad", parsed.acqNameEn)
        assertEquals("", parsed.acqAddressFa)
        assertEquals("", parsed.acqAddressEn)
        assertEquals("021-22598009", parsed.tel)
        assertEquals("0000111111", parsed.postalCode)
        assertEquals("00", parsed.linesCount)
        assertEquals("", parsed.headlineNo)
        assertEquals("", parsed.taxMemoryUniqueCode)
        assertEquals("", parsed.salesFundDeviceSerial)
        assertEquals("", parsed.salesFundMemorySerial)
    }

    @Test
    fun terminalInitializer_readsHeadlineListThenUniqueCode() {
        val data = "34091903" +
            "000000131678530" +
            "006" + "96A897" +
            "005" + "sadad" +
            "000" +
            "000" +
            "012" + "021-22598009" +
            "0000111111" +
            "02" + "04" + "1111" + "03" + "222" +
            "06" + "ABC123" +
            "04" + "SER1" +
            "04" + "MEM1"

        val parsed = SadadTerminalInitializerCodec.parse(data)!!

        assertEquals("02", parsed.linesCount)
        assertEquals("1111,222", parsed.headlineNo)
        assertEquals("ABC123", parsed.taxMemoryUniqueCode)
        assertEquals("SER1", parsed.salesFundDeviceSerial)
        assertEquals("MEM1", parsed.salesFundMemorySerial)
    }

    @Test
    fun terminalInitializer_zeroLengthHeadlineStopsList() {
        val data = "34091903" + "000000131678530" +
            "000" + "000" + "000" + "000" + "000" +
            "0000111111" +
            "03" + "00" +
            "06" + "ABC123"

        val parsed = SadadTerminalInitializerCodec.parse(data)!!

        assertEquals("", parsed.headlineNo)
        assertEquals("ABC123", parsed.taxMemoryUniqueCode)
    }

    @Test
    fun iranSystemHex_96A897_isTest() {
        assertEquals(
            "تست",
            IranSystemEncoding.fieldToUtf8("96A897".toByteArray(Charsets.ISO_8859_1)),
        )
    }
}
