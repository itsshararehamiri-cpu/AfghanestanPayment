package com.danesh.sadad.logon

import com.danesh.api.OptionalReceiptLimits
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData
import com.danesh.sadad.util.SadadHostFunctionCodes
import com.danesh.sadad.util.toLimits
import org.junit.Assert.assertEquals
import org.junit.Test

class SadadOptionalReceiptCodecTest {

    @Test
    fun clientFc034MirrorsHostFc033Layout() {
        val limits = OptionalReceiptLimits(active = true, lowerRials = 10_000, upperRials = 500_000)
        val data = SadadLogonMessageBuilder.optionalReceiptData(limits)
        assertEquals("1" + "000000010000" + "000000500000", data)
        val host = SadadHostFunctionCodes.parse(Field63Generator.generate(listOf(FunctionCodeData("033", data))))
        assertEquals(limits, host.optionalReceipt?.toLimits())
    }
}
