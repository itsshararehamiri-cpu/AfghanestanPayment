package com.danesh.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionTransportCodesTest {

    @Test
    fun normalizeCode_mapsLegacyWordCodesToNumeric() {
        assertEquals(TransactionTransportCodes.QUEUE_BLOCKED, TransactionTransportCodes.normalizeCode("QUEUE"))
        assertEquals(TransactionTransportCodes.CONNECT_FAILED, TransactionTransportCodes.normalizeCode("CONN"))
        assertEquals(TransactionTransportCodes.SEND_FAILED, TransactionTransportCodes.normalizeCode("SEND"))
        assertEquals(TransactionTransportCodes.RECEIVE_FAILED, TransactionTransportCodes.normalizeCode("NET"))
    }

    @Test
    fun normalizeCode_keepsNumericCodes() {
        assertEquals(TransactionTransportCodes.CONNECT_FAILED, TransactionTransportCodes.normalizeCode("-3"))
    }

    @Test
    fun isTransientFailure_acceptsLegacyAndNumericCodes() {
        assertTrue(TransactionTransportCodes.isTransientFailure("QUEUE"))
        assertTrue(TransactionTransportCodes.isTransientFailure("-6"))
        assertTrue(TransactionTransportCodes.isTransientFailure("CONN"))
        assertFalse(TransactionTransportCodes.isTransientFailure("12"))
    }
}
