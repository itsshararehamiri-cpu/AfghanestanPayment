package com.danesh.common.card

import com.danesh.core.emv.ContactlessCardData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KahrobaPinRuleTest {

    @Test
    fun `balance always needs pin`() {
        assertTrue(kahrobaPinRequired(ContactlessReadRequest.Balance, cardRequestsPin = false, noPinAmountLimit = 1_000_000))
    }

    @Test
    fun `purchase below limit without card cvm skips pin`() {
        assertFalse(kahrobaPinRequired(ContactlessReadRequest.Purchase("500000"), cardRequestsPin = false, noPinAmountLimit = 1_000_000))
    }

    @Test
    fun `purchase above limit or card cvm needs pin`() {
        assertTrue(kahrobaPinRequired(ContactlessReadRequest.Purchase("1,000,001"), cardRequestsPin = false, noPinAmountLimit = 1_000_000))
        assertTrue(kahrobaPinRequired(ContactlessReadRequest.Purchase("1000"), cardRequestsPin = true, noPinAmountLimit = 1_000_000))
        // سقف صفر یعنی همیشه PIN
        assertTrue(kahrobaPinRequired(ContactlessReadRequest.Purchase("1000"), cardRequestsPin = false, noPinAmountLimit = 0))
    }

    @Test
    fun `card session keeps kahroba data for same track2 only`() {
        val session = CardSession()
        val card = ContactlessCardData(
            track2 = "6037991234567890=2512",
            pan = "6037991234567890",
            iccData = "9F2608AA",
            cardRequestsPin = false,
        )
        session.setKahroba(card, pinRequired = false)
        assertEquals(CardEntryMode.KAHROBA, session.entryMode)

        session.set(card.track2, card.pan)
        assertEquals("9F2608AA", session.iccData)

        session.markKahrobaPinRequired()
        assertTrue(session.kahroba!!.pinRequired)

        session.set("5022291234567890=2512", "5022291234567890")
        assertNull(session.kahroba)
        assertEquals(CardEntryMode.MAGNETIC, session.entryMode)
        assertEquals("", session.iccData)
    }
}
