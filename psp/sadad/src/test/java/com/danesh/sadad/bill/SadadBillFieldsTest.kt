package com.danesh.sadad.bill

import com.danesh.sadad.util.SadadResponseCodeText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SadadBillFieldsTest {

    @Test
    fun field48_leftPadsBillIdAndPaymentIdTo13() {
        assertEquals(
            "0000000123456" + "0000132045203",
            SadadBillFields.field48("123456", "132045203"),
        )
    }

    @Test
    fun amountFromPaymentId_appendsThreeLowDigits() {
        assertEquals("000001320000", SadadBillFields.amountFromPaymentId("132045203"))
    }

    @Test
    fun amountFromPaymentId_rejectsShorterThanMinimum() {
        assertEquals("000000000000", SadadBillFields.amountFromPaymentId("12345"))
    }

    @Test
    fun resolvePaymentAmount_usesEnteredNonZeroAmount() {
        assertEquals("000000005000", SadadBillFields.resolvePaymentAmount("5000", "132045203"))
    }

    @Test
    fun resolvePaymentAmount_derivesFromPaymentIdWhenEnteredIsBlankOrZero() {
        assertEquals("000001320000", SadadBillFields.resolvePaymentAmount("0", "132045203"))
        assertEquals("000001320000", SadadBillFields.resolvePaymentAmount("", "132045203"))
        assertEquals("000001320000", SadadBillFields.resolvePaymentAmount("0000", "132045203"))
    }

    @Test
    fun inquiryIdsFromSingleIdentifier_mapsToBillIdAndEmptyPaymentId() {
        val ids = SadadBillFields.inquiryIdsFromSingleIdentifier("09121234567")
        assertEquals("09121234567" to "", ids)
        assertEquals(
            "0009121234567" + "0000000000000",
            SadadBillFields.field48(ids.first, ids.second),
        )
    }

    @Test
    fun parseField48_splitsThirteenPlusThirteen() {
        val parsed = SadadBillFields.parseField48("0000000123456" + "0000132045203")
        assertEquals("123456" to "132045203", parsed)
    }

    @Test
    fun parseField48_rejectsShortPayload() {
        assertNull(SadadBillFields.parseField48("123456"))
    }

    @Test
    fun inquiryField63_isFunctionCode8WithEmptyData() {
        assertEquals("01008000", SadadBillFields.inquiryField63())
    }

    @Test
    fun responseCodeResourceName_padsToTwoDigits() {
        assertEquals("sadad_de39_00", SadadResponseCodeText.resourceName("0"))
        assertEquals("sadad_de39_00", SadadResponseCodeText.resourceName("00"))
        assertEquals("sadad_de39_20", SadadResponseCodeText.resourceName("20"))
        assertNull(SadadResponseCodeText.resourceName("-2"))
        assertNull(SadadResponseCodeText.resourceName(""))
    }
}
