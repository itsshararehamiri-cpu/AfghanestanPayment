package com.danesh.sadad.bill

import com.danesh.api.BillPaymentFieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadBillIdValidatorTest {

    @Test
    fun validPair_extractsAmountAndBillType() {
        val billId = billWithCheck("12341")
        val paymentId = paymentWithChecks(billId, "1320452")
        val result = SadadBillIdValidator.validate(billId, paymentId)

        assertTrue(result.isValid)
        assertEquals(1, result.billType)
        assertNull(result.billIdError)
        assertNull(result.paymentIdError)
        assertEquals(SadadBillFields.amountFromPaymentId(paymentId), result.amount)
        assertEquals("000001320000", result.amount)
    }

    @Test
    fun billType_readsSecondToLastDigit() {
        assertEquals(0, SadadBillIdValidator.billType("123401"))
        assertEquals(2, SadadBillIdValidator.billType("123421"))
        assertEquals(5, SadadBillIdValidator.billType("123451"))
        assertEquals(6, SadadBillIdValidator.billType("123461"))
        assertEquals(6, SadadBillIdValidator.billType("123471"))
        assertEquals(8, SadadBillIdValidator.billType("123481"))
        assertEquals(9, SadadBillIdValidator.billType("123491"))
        assertEquals(-4, SadadBillIdValidator.billType("1"))
    }

    @Test
    fun rejectsShortZeroAndBadBillCheckDigit() {
        assertEquals(BillPaymentFieldError.EMPTY, SadadBillIdValidator.validate("", "123456").billIdError)
        assertEquals(BillPaymentFieldError.INVALID, SadadBillIdValidator.validate("12345", "123456").billIdError)
        assertEquals(BillPaymentFieldError.INVALID, SadadBillIdValidator.validate("000000", "123456").billIdError)

        val billId = billWithCheck("12341")
        val broken = billId.dropLast(1) + if (billId.last() == '0') '1' else '0'
        assertEquals(BillPaymentFieldError.CHECK_DIGIT, SadadBillIdValidator.validate(broken, "123456").billIdError)
    }

    @Test
    fun rejectsPaymentLengthCheckDigitAndPair() {
        val billId = billWithCheck("12341")
        val paymentId = paymentWithChecks(billId, "1320452")

        assertEquals(
            BillPaymentFieldError.INVALID,
            SadadBillIdValidator.validate(billId, "12345").paymentIdError,
        )
        assertEquals(
            BillPaymentFieldError.INVALID,
            SadadBillIdValidator.validate(billId, "000000").paymentIdError,
        )

        val badFirstCheck = paymentId.dropLast(2) +
            (if (paymentId[paymentId.length - 2] == '0') '1' else '0') +
            paymentId.last()
        assertEquals(
            BillPaymentFieldError.CHECK_DIGIT,
            SadadBillIdValidator.validate(billId, badFirstCheck).paymentIdError,
        )

        val badPair = paymentId.dropLast(1) + if (paymentId.last() == '0') '1' else '0'
        assertEquals(
            BillPaymentFieldError.PAIR,
            SadadBillIdValidator.validate(billId, badPair).paymentIdError,
        )
    }

    @Test
    fun checkDigit_resetsWeightAfterSeven() {
        assertFalse(SadadBillIdValidator.checkDigitMismatch("1234510".padStart(13, '0')))
    }

    private fun billWithCheck(coreAndType: String): String {
        for (digit in '0'..'9') {
            val id = coreAndType + digit
            if (!SadadBillIdValidator.checkDigitMismatch(id.padStart(13, '0'))) return id
        }
        error("no bill check digit for $coreAndType")
    }

    private fun paymentWithChecks(billId: String, amountYearPeriod: String): String {
        for (first in '0'..'9') {
            val withFirst = amountYearPeriod + first
            if (SadadBillIdValidator.checkDigitMismatch(withFirst)) continue
            for (second in '0'..'9') {
                val paymentId = withFirst + second
                if (!SadadBillIdValidator.checkDigitMismatch(billId + paymentId)) return paymentId
            }
        }
        error("no payment check digits for $amountYearPeriod")
    }
}
