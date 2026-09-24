package com.danesh.sadad.queue

import com.danesh.api.AdviceReverseResult
import com.danesh.api.TransactionTransportCodes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadSafResponseCodesTest {

    @Test
    fun adviceAndReverseSuccessCodesRemoveFromQueue() {
        val policy = SadadQueueRemovalPolicy()
        listOf("0", "00", "2", "02", "5", "05", "25").forEach { code ->
            assertTrue(SadadSafResponseCodes.isSuccess(code))
            assertTrue(
                policy.shouldRemoveFromQueue(
                    AdviceReverseResult(isSuccess = false, responseCode = code),
                ),
            )
        }
    }

    @Test
    fun otherHostCodeIsNotSafSuccess() {
        assertFalse(SadadSafResponseCodes.isSuccess("12"))
        assertFalse(SadadSafResponseCodes.isSuccess("80"))
        assertFalse(SadadSafResponseCodes.isSuccess(null))
    }

    @Test
    fun transportFailureStaysInQueue() {
        val policy = SadadQueueRemovalPolicy()
        assertFalse(
            policy.shouldRemoveFromQueue(
                AdviceReverseResult(
                    isSuccess = false,
                    responseCode = TransactionTransportCodes.RECEIVE_FAILED,
                ),
            ),
        )
    }
}
