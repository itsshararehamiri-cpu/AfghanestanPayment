package com.danesh.iso.packager

import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HpTerminalConfigResponsePackagerTest {

    /** پاسخ واقعی 1314 سوییچ کارن به درخواست پیکربندی ترمینال (DE43 با طول دو بایتی). */
    private val terminalConfigResponseHex =
        "13148230014002E00000010000000000000009121016520000042609121016503050601203001234567848504130303034" +
            "303033303032303000214B6172656E20537769746368204D65726368616E740319303031303034544346473030323030" +
            "333030313030393030313130313030303131303131303634423246374145414242323641343532433139303936454430" +
            "373845383846383733343743463041413232333445314641363730304441373645394639424635303031323031343230" +
            "323630383236303030303030303230303033393731303231303033414647303232303035656E2D4146303233303130" +
            "417369612F4B6162756C3033303031324B6172656E205377697463683033313030395468616E6B20796F753034303034" +
            "3950555243484153452C42414C414E43452C434153485F494E2C434153485F4F55542C4332432C4332572C5732432C57" +
            "325730343130303944454641554C543031303530303032333030353130303132303532303032363030363030303742" +
            "415443483031303730303036434F4D4D3031"

    @Test
    fun unpack_terminalConfigResponse_readsActionCodeAndAcceptorFields() {
        val msg = ISOMsg().apply { packager = HpTerminalConfigResponsePackager() }

        msg.unpack(ISOUtil.hex2byte(terminalConfigResponseHex))

        assertEquals("1314", msg.mti)
        assertEquals("305", msg.getString(24))
        assertEquals("6012", msg.getString(26))
        assertEquals("300", msg.getString(39))
        assertEquals("12345678", msg.getString(41))
        assertEquals("HPA000400300200", msg.getString(42))
        assertEquals("Karen Switch Merchant", msg.getString(43))
        assertEquals(319, msg.getString(72).length)
        assertEquals("001004TCFG", msg.getString(72).take(10))
    }

    @Test
    fun unpack_terminalConfigResponse_failsWithDefaultHpPackager() {
        val msg = ISOMsg().apply { packager = HpIso93BPackager() }

        assertThrows(ISOException::class.java) {
            msg.unpack(ISOUtil.hex2byte(terminalConfigResponseHex))
        }
    }
}
