package com.danesh.bp.init

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.KeyFactory
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class BpInitRsaSessionTest {

    @Test
    fun sanitizeToken_stripsWhitespaceAndControlChars() {
        assertEquals("ABC123", BpInitRsaSession.sanitizeToken("  ABC 123 \r\n\t"))
        assertEquals("TICKET", BpInitRsaSession.sanitizeToken("\uFEFFTICKET\n"))
    }

    @Test
    fun calculatePor_matchesOfficialTicketPlusSerial() {
        // PoR = SHA256(Ticket_1 + Serial) — مستند به‌پرداخت
        assertEquals(
            "0A603D58119F5828DF7F4A90B3C563DAB431E86ED8EB6EC35027384405230579",
            BpInitRsaSession.calculatePor("185541303", "D1V2890000001"),
        )
    }

    @Test
    fun calculatePor_matchesWireCaptureTicketPlusSerial() {
        assertEquals(
            "0CE6EEF5DA9E16DACD7E92A3E75D1EEE77D5A2918499056798F07AC0E6F773BC",
            BpInitRsaSession.calculatePor("185541303", "MA2E2351269195"),
        )
        assertEquals(
            "0CE6EEF5DA9E16DACD7E92A3E75D1EEE77D5A2918499056798F07AC0E6F773BC",
            BpInitRsaSession.formatField61(
                BpInitRsaSession.calculatePor("185541303", "MA2E2351269195"),
            ),
        )
    }

    @Test
    fun legacyWrongFormula_ticketOnly_isNotUsed() {
        // مقدار اشتباه قبلی: SHA256(Ticket_1) alone → B1169466...
        assertFalse(
            BpInitRsaSession.calculatePor("185541303", "D1V2890000001") ==
                "B1169466EEC88508B8371C5899695A431F7B7940DBD225CCF20E21EB507DB121",
        )
    }

    @Test
    fun formatField61_returnsPorOnly() {
        val por = BpInitRsaSession.calculatePor("TICKET1", "D1V2890000001")
        assertEquals(por, BpInitRsaSession.formatField61(por))
        assertFalse(BpInitRsaSession.formatField61(por).contains("@"))
    }

    @Test
    fun verifyPoa_matchesExpectedHash() {
        val ticket2 = "TICKET2"
        val serial = "D1V2890000001"
        val poa = BpInitRsaSession.calculatePoa(ticket2, serial)
        assertTrue(
            BpInitRsaSession.verifyPoa(
                ticket2 = ticket2,
                deviceSerial = serial,
                receivedPoa = poa,
            ),
        )
        assertTrue(
            BpInitRsaSession.verifyPoa(
                ticket2 = ticket2,
                deviceSerial = serial,
                receivedPoa = poa.lowercase(),
            ),
        )
        assertFalse(
            BpInitRsaSession.verifyPoa(
                ticket2 = ticket2,
                deviceSerial = serial,
                receivedPoa = "INVALID",
            ),
        )
    }

    @Test
    fun parseResponseField61Poa_acceptsPoaOnlyOrSerialAtPoa() {
        val serial = "D1V2890000001"
        val poa = BpInitRsaSession.calculatePoa("806366405", serial)
        assertEquals(poa, BpInitRsaSession.parseResponseField61Poa(poa, serial))
        assertEquals(
            poa,
            BpInitRsaSession.parseResponseField61Poa("$serial@$poa", serial),
        )
    }

    @Test
    fun calculatePoa_matchesOfficialTicket2PlusSerial() {
        assertEquals(
            "041035253E70AF7850E16ADBFDEFDD4BB685B1507E0FBD34BD2DDC14B26C7EA4",
            BpInitRsaSession.calculatePoa("806366405", "D1V2890000001"),
        )
        assertEquals(64, BpInitRsaSession.calculatePoa("806366405", "D1V2890000001").length)
    }

    @Test
    fun calculatePoa_matchesWireCaptureTicket2PlusSerial() {
        assertEquals(
            "5D86F25CC2EE5DE16275A756EE285D7DD6C8A38457054DFFB540D324692FE41E",
            BpInitRsaSession.calculatePoa("806366405", "MA2E2351269195"),
        )
    }

    @Test
    fun prepareKeyPair_rsa2048_spkiDerForField62() {
        val session = BpInitRsaSession()
        val sessionId = session.prepareKeyPair()
        val der = session.publicKeyDer(sessionId)
        assertEquals(BpInitRsaSession.EXPECTED_SPKI_DER_LENGTH, der.size)
        assertEquals(0x30.toByte(), der[0])
        assertEquals(0x82.toByte(), der[1])
        assertEquals(0x01.toByte(), der[2])
        assertEquals(0x22.toByte(), der[3])
        BpInitRsaSession.validatePublicKeyDer(der)
        assertEquals(64, session.fingerprint(sessionId).length)
    }

    @Test
    fun publicKeyDer_requiresPrepareKeyPairFirst() {
        val session = BpInitRsaSession()
        try {
            session.publicKeyDer("missing-session")
            fail("Expected error when prepareKeyPair was not called")
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("prepareKeyPair"))
        }
    }

    @Test
    fun prepareKeyPair_keepsPreviousSessionUntilCleared() {
        val session = BpInitRsaSession()
        val firstId = session.prepareKeyPair()
        val firstDer = session.publicKeyDer(firstId)
        val secondId = session.prepareKeyPair()
        val secondDer = session.publicKeyDer(secondId)
        assertFalse(firstId == secondId)
        assertFalse(firstDer.contentEquals(secondDer))
        // کلید اول هنوز برای decrypt پاسخ دیررس موجود است
        assertEquals(2, session.activeSessionCount())
        assertArrayEquals(firstDer, session.publicKeyDer(firstId))
    }

    @Test
    fun concurrentInit_secondPrepareDoesNotBreakFirstDecrypt() {
        val session = BpInitRsaSession()
        val firstId = session.prepareKeyPair()
        session.bindToStan(firstId, "000001")
        val firstPub = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(session.publicKeyDer(firstId)))

        val secondId = session.prepareKeyPair()
        session.bindToStan(secondId, "000002")

        val plaintext = "TERMINAL-KEY-A".toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            firstPub,
            OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT,
            ),
        )
        val encryptedForFirst = cipher.doFinal(plaintext)

        // Init دوم آمده؛ پاسخ اول هنوز باید با private اول باز شود
        assertArrayEquals(plaintext, session.decryptField62(encryptedForFirst, "000001"))
        session.clearByStan("000001")
        assertEquals(1, session.activeSessionCount())
    }

    @Test
    fun decryptField62_roundTripWithOaepSha256() {
        val session = BpInitRsaSession()
        val sessionId = session.prepareKeyPair()
        session.bindToStan(sessionId, "000320")
        val plaintext = "TERMINAL-KEY-PAYLOAD".toByteArray(Charsets.UTF_8)
        val pub = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(session.publicKeyDer(sessionId)))
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            pub,
            OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT,
            ),
        )
        val encrypted = cipher.doFinal(plaintext)
        assertArrayEquals(plaintext, session.decryptField62(encrypted, "000320"))
        assertArrayEquals(
            plaintext,
            session.decryptOaepSha256(encrypted, session.peekPrivateKey(sessionId)),
        )
    }

    /**
     * Offline: Public Key DER (مثل F62 Init) → encrypt OAEP-SHA256 → decrypt با private session.
     */
    @Test
    fun rsa_encrypt_decrypt_test() {
        val session = BpInitRsaSession()
        val sessionId = session.prepareKeyPair()
        session.bindToStan(sessionId, "000001")
        val publicKeyDer = session.publicKeyDer(sessionId)

        val pub = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(publicKeyDer))

        val data = "HELLO".toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            pub,
            OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT,
            ),
        )
        val encrypted = cipher.doFinal(data)

        val decrypted = session.decryptOaepSha256(encrypted, session.peekPrivateKey(sessionId))
        assertEquals("HELLO", decrypted.toString(Charsets.UTF_8))
        assertEquals("HELLO", session.decryptField62(encrypted, "000001").toString(Charsets.UTF_8))
    }

    @Test
    fun clearByStan_removesOnlyThatSession() {
        val session = BpInitRsaSession()
        val firstId = session.prepareKeyPair()
        val secondId = session.prepareKeyPair()
        session.bindToStan(firstId, "000001")
        session.bindToStan(secondId, "000002")
        session.clearByStan("000001")
        assertEquals(1, session.activeSessionCount())
        try {
            session.decryptField62(ByteArray(256), "000001")
            fail("Expected decrypt to fail after clearByStan")
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("RSA session"))
        }
        // نشست دوم سالم است
        assertArrayEquals(
            session.publicKeyDer(secondId),
            session.publicKeyDer(secondId),
        )
    }
}
