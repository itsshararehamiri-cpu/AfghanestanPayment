package com.danesh.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultQueueRemovalPolicyTest {

    @Test
    fun removesOnSuccess() {
        assertTrue(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = true, responseCode = "00"),
            ),
        )
    }

    @Test
    fun keepsOnRetry80() {
        assertFalse(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = false, responseCode = "80"),
            ),
        )
    }

    @Test
    fun removesOnOtherHostField39() {
        assertTrue(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = false, responseCode = "12"),
            ),
        )
    }

    @Test
    fun keepsOnTransportFailures() {
        assertFalse(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = false, responseCode = "CONN"),
            ),
        )
        assertFalse(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = false, responseCode = "SEND"),
            ),
        )
        assertFalse(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = false, responseCode = "NET"),
            ),
        )
        assertFalse(
            DefaultQueueRemovalPolicy.shouldRemoveFromQueue(
                AdviceReverseResult(isSuccess = false, responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
            ),
        )
    }
}
