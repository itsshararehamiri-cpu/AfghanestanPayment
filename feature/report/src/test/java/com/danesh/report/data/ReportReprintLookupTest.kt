package com.danesh.report.data

import com.danesh.database.entity.TransactionReportEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReportReprintLookupTest {

    @Test
    fun findEntity_byStanOnly_matchesPaddedTrace() {
        val entities = listOf(
            sampleEntity(id = 1, stan = "000042", rrn = "111111111111"),
            sampleEntity(id = 2, stan = "000099", rrn = "222222222222"),
        )

        val found = ReportReprintLookup.findEntity(
            entities = entities,
            trackingNumber = "42",
            referenceNumber = "",
        )

        assertNotNull(found)
        assertEquals(1L, found?.id)
    }

    @Test
    fun findEntity_byRrnOnly_findsTransaction() {
        val entities = listOf(
            sampleEntity(id = 1, stan = "000001", rrn = "123456789012"),
        )

        val found = ReportReprintLookup.findEntity(
            entities = entities,
            trackingNumber = "",
            referenceNumber = "123456789012",
        )

        assertNotNull(found)
        assertEquals(1L, found?.id)
    }

    @Test
    fun findEntity_byStanAndRrn_requiresBothToMatch() {
        val entities = listOf(
            sampleEntity(id = 1, stan = "000010", rrn = "111111111111"),
            sampleEntity(id = 2, stan = "000020", rrn = "222222222222"),
        )

        val found = ReportReprintLookup.findEntity(
            entities = entities,
            trackingNumber = "20",
            referenceNumber = "222222222222",
        )

        assertNotNull(found)
        assertEquals(2L, found?.id)
    }

    @Test
    fun findEntity_whenStanMatchesButRrnDoesNot_returnsNull() {
        val entities = listOf(
            sampleEntity(id = 1, stan = "000010", rrn = "111111111111"),
        )

        val found = ReportReprintLookup.findEntity(
            entities = entities,
            trackingNumber = "10",
            referenceNumber = "999999999999",
        )

        assertNull(found)
    }

    @Test
    fun findEntity_returnsNewestWhenMultipleMatch() {
        val entities = listOf(
            sampleEntity(id = 1, stan = "000010", rrn = "111111111111", timestamp = 100L),
            sampleEntity(id = 2, stan = "000010", rrn = "111111111111", timestamp = 200L),
        )

        val found = ReportReprintLookup.findEntity(
            entities = entities,
            trackingNumber = "10",
            referenceNumber = "",
        )

        assertEquals(2L, found?.id)
    }

    private fun sampleEntity(
        id: Long,
        stan: String,
        rrn: String,
        timestamp: Long = 1L,
    ) = TransactionReportEntity(
        id = id,
        timestamp = timestamp,
        processingCode = "000000",
        amount = 1000L,
        stan = stan,
        dateTransaction = "20250805",
        timeTransaction = "120000",
        merchantId = "1",
        maskedPan = "6219****1234",
        type = 0,
        rrn = rrn,
        issuer = null,
        responseCode = 0,
        responseMsg = null,
        terminalId = "12345678",//
    )
}
