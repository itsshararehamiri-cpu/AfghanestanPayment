package com.danesh.sadad.util

import com.danesh.api.HostPrintTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadPrintDataParserTest {

    /** نمونهٔ واقعی DE63 کالابرگ سداد (بدون دو بایت طول BCD ابتدای فیلد). */
    private val sampleField63Hex =
        "3031303239313037303330333332353A29F191FEA428EFA493F291EEFFA590FFFC97A190A2A495FF" +
            "30378080802C80808230333331343A29F191FEA428F1EEFFE6F393F530378282822C828282" +
            "30333331383AEFA493FFF291EEFFFDA4FEF0FE95FFA2EE313384898584868383888480848281"

    private fun hexToLatin1(hex: String): String =
        String(hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray(), Charsets.ISO_8859_1)

    @Test
    fun parsesSadadCommodityBasketSample() {
        val host = SadadHostFunctionCodes.parse(hexToLatin1(sampleField63Hex))
        val items = host.printItems
        assertEquals(3, items.size)
        assertEquals("پرداختی از کالابرگ(ریال):", items[0].caption)
        assertEquals("۲۰۰,۰۰۰", items[0].value)
        assertEquals("مبلغ کل(ریال):", items[1].caption)
        assertEquals("۲۲۲,۲۲۲", items[1].value)
        assertEquals("کد پیگیری کالا برگ:", items[2].caption)
        assertEquals("۱۲۴۰۴۸۳۳۶۴۵۹۴", items[2].value)
        assertTrue(items.all { it.target == HostPrintTarget.BOTH })
        assertTrue(items.none { it.keyValue })
    }

    @Test
    fun truncatedOrGarbageNeverCrashes() {
        assertTrue(SadadPrintDataParser.parse("").isEmpty())
        assertTrue(SadadPrintDataParser.parse("xx").isEmpty())
        assertTrue(SadadPrintDataParser.parse("05013").isEmpty())
        // آیتم اول سالم، دومی ناقص
        val partial = SadadPrintDataParser.parse("02" + "011" + "02AB" + "02CD" + "011" + "09ABC")
        assertEquals(1, partial.size)
        assertEquals("BA", partial[0].caption)
        assertEquals("DC", partial[0].value)
        assertEquals(HostPrintTarget.CUSTOMER, partial[0].target)
        assertFalse(partial.isEmpty())
    }
}
