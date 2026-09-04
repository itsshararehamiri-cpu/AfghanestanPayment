package com.danesh.report.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTransactionChipFilterTest {

    @Test
    fun visibleFor_bpFeatures_includesVoucherExcludesTransfer() {
        val chips = ReportTransactionChipFilter.visibleFor(
            setOf("PURCHASE", "TOPUP", "BILL", "BALANCE", "SUPPORT", "SETTINGS", "REPORT", "VOUCHER"),
        )
        assertTrue(ReportTransactionChipFilter.ALL in chips)
        assertTrue(ReportTransactionChipFilter.PURCHASE in chips)
        assertTrue(ReportTransactionChipFilter.VOUCHER in chips)
        assertFalse(ReportTransactionChipFilter.BALANCE in chips)
        assertFalse(ReportTransactionChipFilter.TRANSFER in chips)
        assertFalse(ReportTransactionChipFilter.CASH_DEPOSIT in chips)
    }

    @Test
    fun visibleFor_hpFeatures_includesTransferExcludesVoucher() {
        val chips = ReportTransactionChipFilter.visibleFor(
            setOf(
                "TRANSFER",
                "CASH_DEPOSIT",
                "CASH_OUT",
                "SETTINGS",
                "REPORT",
                "PURCHASE",
                "TOPUP",
                "BILL",
                "BALANCE",
            ),
        )
        assertTrue(ReportTransactionChipFilter.TRANSFER in chips)
        assertTrue(ReportTransactionChipFilter.CASH_DEPOSIT in chips)
        assertTrue(ReportTransactionChipFilter.CASH_OUT in chips)
        assertFalse(ReportTransactionChipFilter.VOUCHER in chips)
        assertFalse(ReportTransactionChipFilter.SUPPORT in chips)
        assertEquals(
            listOf(
                ReportTransactionChipFilter.ALL,
                ReportTransactionChipFilter.PURCHASE,
                ReportTransactionChipFilter.BILL,
                ReportTransactionChipFilter.TOPUP,
                ReportTransactionChipFilter.TRANSFER,
                ReportTransactionChipFilter.CASH_DEPOSIT,
                ReportTransactionChipFilter.CASH_OUT,
            ),
            chips,
        )
    }
}
