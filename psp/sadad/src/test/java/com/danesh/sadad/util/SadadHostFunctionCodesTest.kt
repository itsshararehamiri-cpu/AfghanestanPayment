package com.danesh.sadad.util

import com.danesh.api.TransactionResultDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadHostFunctionCodesTest {

    private fun field63(vararg blocks: Pair<String, String>): String =
        Field63Generator.generate(blocks.map { (code, data) -> FunctionCodeData(code, data) })

    @Test
    fun emptyOrZeroCount_isEmpty() {
        assertTrue(SadadHostFunctionCodes.parse("").isEmpty)
        assertTrue(SadadHostFunctionCodes.parse("00").isEmpty)
        assertTrue(SadadHostFunctionCodes.parse(null as String?).isEmpty)
    }

    @Test
    fun parsesDiscountBillInquiryAndWinners() {
        val host = SadadHostFunctionCodes.parse(
            field63(
                "007" to "000000100000" + "000000090000" + "000000089000",
                "008" to "000001898000" + "323938233373" + "20260924" + "065106" + "14" + "003008",
                "003" to "96A897",
                "004" to "96A897",
            ),
        )
        assertEquals(SadadHostData.Discount("000000100000", "000000090000", "000000089000"), host.discount)
        assertEquals("000001898000", host.billInquiry?.amount)
        assertEquals("323938233373", host.billInquiry?.referenceNumber)
        assertEquals("003008", host.billInquiry?.systemTraceNo)
        assertEquals("تست", host.cardHolderWinnerMessage)
        assertEquals("تست", host.merchantWinnerMessage)
    }

    @Test
    fun parsesTopupVatOrganizationAndOptionalReceipt() {
        val host = SadadHostFunctionCodes.parse(
            field63(
                "018" to "2" + "0001000",
                "026" to "006" + "96A897" + "004" + "TEST",
                "033" to "1" + "000000010000" + "000000500000",
            ),
        )
        assertEquals(10.0, host.topupVatPercent!!, 0.0001)
        assertEquals(SadadHostData.BillOrganization("تست", "TEST"), host.billOrganization)
        assertEquals(SadadHostData.OptionalReceipt(true, "000000010000", "000000500000"), host.optionalReceipt)
    }

    @Test
    fun malformedCodeIsIgnoredOthersKept() {
        val host = SadadHostFunctionCodes.parse(
            field63("007" to "12", "003" to "96A897", "013" to "whatever"),
        )
        assertNull(host.discount)
        assertEquals("تست", host.cardHolderWinnerMessage)
    }

    @Test
    fun optionalReceiptInactive() {
        val host = SadadHostFunctionCodes.parse(field63("033" to "0"))
        assertFalse(host.optionalReceipt!!.isActive)
    }

    @Test
    fun receiptLinesAndMerchantOnlyText() {
        val host = SadadHostData(
            billOrganization = SadadHostData.BillOrganization("آب", ""),
            discount = SadadHostData.Discount("000000100000", "000000090000", "000000089000"),
            cardHolderWinnerMessage = "برنده شدید",
            merchantWinnerMessage = "پذیرنده برنده شد",
        )
        val detail = SadadHostDataReceipt.apply(
            TransactionResultDetail(hostReceiptTextSecond = "قبلی"),
            host,
        )
        assertEquals(
            "قبلی\nسازمان: آب\nمبلغ اصلی: 100,000 ریال\nمبلغ پس از تخفیف: 90,000 ریال\nبرنده شدید",
            detail.hostReceiptTextSecond,
        )
        assertEquals("پذیرنده برنده شد", detail.merchantReceiptText)
    }

    @Test
    fun billInquiryAmountFillsEmptyAmountOnly() {
        val host = SadadHostData(
            billInquiry = SadadHostData.BillInquiry("000001898000", "", "", "", "", ""),
        )
        assertEquals("1898000", SadadHostDataReceipt.apply(TransactionResultDetail(amount = ""), host).amount)
        assertEquals("5000", SadadHostDataReceipt.apply(TransactionResultDetail(amount = "5000"), host).amount)
    }
}
