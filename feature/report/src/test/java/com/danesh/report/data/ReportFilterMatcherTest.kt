package com.danesh.report.data

import com.danesh.database.entity.TransactionReportEntity
import com.danesh.report.data.ReportFilterMatcher
import com.danesh.report.data.normalizedForQuery
import com.danesh.report.model.ReportFilterState
import com.danesh.report.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportFilterMatcherTest {

    @Test
    fun `tracking number matches partial stan`() {
        val entity = sampleEntity(stan = "000123")
        val filters = ReportFilterState(trackingNumber = "123")
        assertTrue(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `time range includes entity time`() {
        val entity = sampleEntity(timeTransaction = "223045")
        val filters = ReportFilterState(fromTime = "22:00", toTime = "23:00")
        assertTrue(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `time range excludes entity time outside bounds`() {
        val entity = sampleEntity(timeTransaction = "223045")
        val filters = ReportFilterState(fromTime = "23:00", toTime = "23:59")
        assertFalse(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `amount range includes entity amount`() {
        val entity = sampleEntity(amount = 50_000)
        val filters = ReportFilterState(fromAmount = "10000", toAmount = "100000")
        assertTrue(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `amount range excludes entity amount below minimum`() {
        val entity = sampleEntity(amount = 5_000)
        val filters = ReportFilterState(fromAmount = "10000")
        assertFalse(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `combined date and time range includes entity datetime`() {
        val entity = sampleEntity(dateTransaction = "20260524", timeTransaction = "153045")
        val filters = ReportFilterState(
            fromDate = "20260524",
            toDate = "20260524",
            fromTime = "15:00",
            toTime = "16:00",
        )
        assertTrue(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `combined date and time range excludes entity outside time window`() {
        val entity = sampleEntity(dateTransaction = "20260524", timeTransaction = "170000")
        val filters = ReportFilterState(
            fromDate = "20260524",
            toDate = "20260524",
            fromTime = "15:00",
            toTime = "16:00",
        )
        assertFalse(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `date range includes entity date`() {
        val entity = sampleEntity(dateTransaction = "20260524")
        val filters = ReportFilterState(fromDate = "20260501", toDate = "20260531")
        assertTrue(ReportFilterMatcher.matches(entity, filters))
    }

    @Test
    fun `normalized query swaps inverted amount range`() {
        val filters = ReportFilterState(fromAmount = "9000", toAmount = "1000").normalizedForQuery()
        assertEquals("1000", filters.fromAmount)
        assertEquals("9000", filters.toAmount)
    }

    @Test
    fun `failed status excludes successful transaction`() {
        val entity = sampleEntity(responseCode = 0)
        val filters = ReportFilterState(transactionStatus = TransactionStatus.FAILED)
        assertFalse(ReportFilterMatcher.matches(entity, filters))
    }

    private fun sampleEntity(
        stan: String = "1",
        rrn: String? = null,
        dateTransaction: String = "20260101",
        timeTransaction: String = "120000",
        amount: Long = 1000,
        responseCode: Int? = 0,
        processingCode: String = "000000",
        type: Int = 1,
    ) = TransactionReportEntity(
        id = 1,
        timestamp = 1L,
        processingCode = processingCode,
        amount = amount,
        stan = stan,
        dateTransaction = dateTransaction,
        timeTransaction = timeTransaction,
        merchantId = "m",
        maskedPan = null,
        type = type,
        rrn = rrn,
        issuer = null,
        responseCode = responseCode,
        responseMsg = null,
        terminalId = "t",
    )
}
