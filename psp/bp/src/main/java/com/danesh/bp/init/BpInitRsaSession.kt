package com.danesh.bp.init

import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpInitRsaSession @Inject constructor() {

    private data class LiveSession(
        val sessionId: String,
        val keyPair: KeyPair,
        val publicKeyDer: ByteArray,
        val fingerprintSha256: String,
        val createdAtMs: Long,
        @Volatile var stan: String? = null,
    )

    private val sessionsById = ConcurrentHashMap<String, LiveSession>()
    private val sessionIdByStan = ConcurrentHashMap<String, String>()


    fun prepareKeyPair(): String {
        purgeExpiredSessions()
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(RSA_KEY_SIZE, SecureRandom())
        val generated = generator.generateKeyPair()
        val publicKey = generated.public as RSAPublicKey
        check(publicKey.modulus.bitLength() == RSA_KEY_SIZE) {
            "طول RSA باید $RSA_KEY_SIZE بیت باشد (بود ${publicKey.modulus.bitLength()})"
        }
        val der = publicKey.encoded.copyOf()
        validatePublicKeyDer(der)
        val fingerprint = fingerprintDerSha256(der)
        val sessionId = UUID.randomUUID().toString()
        sessionsById[sessionId] = LiveSession(
            sessionId = sessionId,
            keyPair = generated,
            publicKeyDer = der,
            fingerprintSha256 = fingerprint,
            createdAtMs = System.currentTimeMillis(),
        )
        enforceMaxSessions()
        BpInitTrace.step(
            "RsaSession",
            "prepareKeyPair sessionId=$sessionId fingerprintSha256=$fingerprint " +
                "derLen=${der.size} activeSessions=${sessionsById.size}",
        )
        return sessionId
    }

    fun publicKeyDer(sessionId: String): ByteArray {
        val session = requireSession(sessionId)
        val der = session.publicKeyDer.copyOf()
        validatePublicKeyDer(der)
        return der
    }

    fun fingerprint(sessionId: String): String = requireSession(sessionId).fingerprintSha256

    fun publicKeyBase64(sessionId: String): String =
        Base64.encodeToString(publicKeyDer(sessionId), Base64.NO_WRAP)

    fun bindToStan(sessionId: String, stan: String) {
        val normalizedStan = stan.trim()
        require(normalizedStan.isNotEmpty()) { "STAN برای bind نشست RSA خالی است" }
        val session = requireSession(sessionId)
        session.stan?.let { previous ->
            if (previous != normalizedStan) {
                sessionIdByStan.remove(previous, sessionId)
            }
        }
        session.stan = normalizedStan
        sessionIdByStan[normalizedStan] = sessionId
        BpInitTrace.step(
            "RsaSession",
            "bindToStan sessionId=$sessionId stan=$normalizedStan " +
                "fingerprintSha256=${session.fingerprintSha256}",
        )
    }


    fun decryptField62(encrypted: ByteArray, stan: String): ByteArray {
        val normalizedStan = stan.trim()
        val sessionId = sessionIdByStan[normalizedStan]
            ?: error(
                "RSA session برای STAN=$normalizedStan یافت نشد — " +
                    "Init دوم کلید اول را overwrite نکرده؛ احتمالاً bind/پاسخ اشتباه است",
            )
        return decryptField62BySessionId(encrypted, sessionId)
    }

    fun decryptField62BySessionId(encrypted: ByteArray, sessionId: String): ByteArray {
        BpInitTrace.step(
            "RsaSession",
            "رمزگشایی فیلد 62 (OAEP SHA-256) sessionId=$sessionId",
        )
        val session = requireSession(sessionId)
        val privateKey = session.keyPair.private
            ?: error("RSA private key is not available")
        val payload = decodeField62Payload(encrypted)
        return decryptOaepSha256(payload, privateKey)
    }

    internal fun decryptOaepSha256(ciphertext: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            privateKey,
            OAEP_PARAMETER_SPEC,
        )
        return cipher.doFinal(ciphertext)
    }

    internal fun peekPrivateKey(sessionId: String): PrivateKey = requireSession(sessionId).keyPair.private

    private fun decodeField62Payload(data: ByteArray): ByteArray {
        if (data.isEmpty()) return data
        val asText = data.toString(Charsets.US_ASCII).trim()
        if (asText.length >= 4 && asText.all { it.isLetterOrDigit() || it == '+' || it == '/' || it == '=' }) {
            return runCatching { Base64.decode(asText, Base64.DEFAULT) }.getOrDefault(data)
        }
        return data
    }

    fun clearSession(sessionId: String) {
        val removed = sessionsById.remove(sessionId) ?: return
        removed.stan?.let { sessionIdByStan.remove(it, sessionId) }
        wipeSessionMaterial(removed)
        BpInitTrace.step(
            "RsaSession",
            "clearSession sessionId=$sessionId remaining=${sessionsById.size}",
        )
    }

    fun clearByStan(stan: String) {
        val normalizedStan = stan.trim()
        if (normalizedStan.isEmpty()) return
        val sessionId = sessionIdByStan.remove(normalizedStan) ?: return
        val removed = sessionsById.remove(sessionId)
        if (removed != null) wipeSessionMaterial(removed)
        BpInitTrace.step(
            "RsaSession",
            "clearByStan sessionId=$sessionId remaining=${sessionsById.size}",
        )
    }

    fun clear() {
        val count = sessionsById.size
        sessionsById.values.forEach(::wipeSessionMaterial)
        sessionsById.clear()
        sessionIdByStan.clear()
        if (count > 0) {
            BpInitTrace.step("RsaSession", "clear all RSA sessions count=$count")
        }
    }

    private fun wipeSessionMaterial(session: LiveSession) {
        session.publicKeyDer.fill(0)
    }

    fun activeSessionCount(): Int = sessionsById.size

    private fun requireSession(sessionId: String): LiveSession =
        sessionsById[sessionId]
            ?: error("قبل از Init باید prepareKeyPair() فراخوانی شود (sessionId=$sessionId)")

    private fun purgeExpiredSessions() {
        val now = System.currentTimeMillis()
        val expired = sessionsById.values.filter { now - it.createdAtMs > SESSION_TTL_MS }
        expired.forEach { clearSession(it.sessionId) }
    }

    private fun enforceMaxSessions() {
        if (sessionsById.size <= MAX_SESSIONS) return
        val oldest = sessionsById.values.sortedBy { it.createdAtMs }
            .take(sessionsById.size - MAX_SESSIONS)
        oldest.forEach { clearSession(it.sessionId) }
    }

    companion object {
        const val RSA_KEY_SIZE = 2048
        const val EXPECTED_SPKI_DER_LENGTH = 294
        private const val MAX_SESSIONS = 8
        private const val SESSION_TTL_MS = 10 * 60 * 1000L
        private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"// "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private val OAEP_PARAMETER_SPEC = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT,
        )

        fun fingerprintDerSha256(der: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(der)
            return digest.toHexUppercase()
        }

        fun validatePublicKeyDer(der: ByteArray) {
            require(der.isNotEmpty()) { "Public Key DER خالی است" }
            require(der[0] == 0x30.toByte()) {
                "Public Key باید SEQUENCE (X.509 SPKI) باشد"
            }
            // RSA-2048 SPKI معمولاً ۲۹۴ بایت است (۰x۳۰ ۰x۸۲ ۰x۰۱ ۰x۲۲ …)
            require(der.size in 270..320) {
                "طول Public Key DER نامعتبر است: ${der.size} (انتظار حدود $EXPECTED_SPKI_DER_LENGTH)"
            }
        }


        fun sanitizeToken(value: String): String =
            value.trim()
                .replace("\uFEFF", "")
                .filterNot { it.isWhitespace() || it.isISOControl() }


        fun calculatePor(ticket1: String, deviceSerial: String): String {
            val ticket = sanitizeToken(ticket1)
            val serial = sanitizeToken(deviceSerial)
            require(ticket.isNotEmpty()) { "Ticket_1 خالی است" }
            require(serial.isNotEmpty()) { "Serial خالی است" }
            return sha256HexUpper(ticket + serial)
        }
        fun calculatePoa(ticket2: String, deviceSerial: String): String {
            val ticket = sanitizeToken(ticket2)
            val serial = sanitizeToken(deviceSerial)
            require(ticket.isNotEmpty()) { "Ticket_2 خالی است" }
            require(serial.isNotEmpty()) { "Serial خالی است" }
            return sha256HexUpper(ticket + serial)
        }
        fun parseResponseField61Poa(field61: String, expectedSerial: String): String {
            val trimmed = field61.trim()
            require(trimmed.isNotBlank()) { "فیلد 61 در پاسخ خالی است" }
            val serial = sanitizeToken(expectedSerial)
            require(serial.isNotEmpty()) { "Serial خالی است" }
            val rawPoa = if (trimmed.contains("@")) {
                val responseSerial = sanitizeToken(trimmed.substringBefore("@"))
                check(responseSerial == serial) { "سریال پاسخ با دستگاه مطابقت ندارد" }
                sanitizeToken(trimmed.substringAfter("@"))
            } else {
                sanitizeToken(trimmed)
            }
            require(rawPoa.isNotBlank()) { "PoA در پاسخ موجود نیست" }
            return normalizePoaHex(rawPoa)
        }

        fun normalizePoaHex(poa: String): String {
            val normalized = sanitizeToken(poa).uppercase()
            require(normalized.length == 64) { "PoA باید ۶۴ کاراکتر hex باشد" }
            require(normalized.all { it.isDigit() || it in 'A'..'F' }) {
                "PoA باید hex معتبر باشد"
            }
            return normalized
        }

        fun verifyPoa(
            ticket2: String,
            deviceSerial: String,
            receivedPoa: String,
        ): Boolean {
            return calculatePoa(ticket2, deviceSerial) == normalizePoaHex(receivedPoa)
        }

        fun formatField61(por: String): String {
            val normalized = sanitizeToken(por).uppercase()
            require(normalized.length == 64) { "PoR باید ۶۴ کاراکتر hex باشد" }
            require(normalized.all { it.isDigit() || it in 'A'..'F' }) {
                "PoR باید hex معتبر باشد"
            }
            return normalized
        }

        fun sha256HexUpper(text: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(text.toByteArray(Charsets.UTF_8))
            return digest.toHexUppercase()
        }

        private fun ByteArray.toHexUppercase(): String =
            joinToString(separator = "") { byte -> "%02X".format(byte) }
    }
}
