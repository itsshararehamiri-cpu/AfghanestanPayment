package com.danesh.bp.field63

import com.danesh.api.PspDeviceMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class BpField63FormatterTest {

    @Test
    fun format_withoutImeiAndSim_keepsTrailingSeparators() {
        val metadata = PspDeviceMetadata(serial = "20634527452", appVersion = "1.2.1")
        assertEquals("20634527452@1.2.1@@", BpField63Formatter.format(metadata))
    }

    @Test
    fun format_withImeiAndSim() {
        val metadata = PspDeviceMetadata(
            serial = "R90634527452",
            appVersion = "1.2.1",
            imei = "490154203237518",
            simSerial = "3498279482349827948",
        )
        assertEquals(
            "R90634527452@1.2.1@490154203237518@3498279482349827948",
            BpField63Formatter.format(metadata),
        )
    }
}
