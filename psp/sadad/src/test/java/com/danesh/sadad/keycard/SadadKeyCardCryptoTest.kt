package com.danesh.sadad.keycard

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher

class SadadKeyCardCryptoTest {

    /**
     * مستند خودِ مقدار e یا نمایش خصوصی کامل (CRT) کارت را نمی‌دهد، فقط Modulus (کلید عمومی)
     * و Private Exponent را جدا از هم برمی‌گرداند. این تست نشان می‌دهد بازسازی کلید خصوصی
     * صرفاً از (modulus, privateExponent) — دقیقا مطابق آنچه کارت برمی‌گرداند — برای
     * رمزگشایی یک متن رمزشده با کلید عمومی متناظر کافی است.
     */
    @Test
    fun buildPrivateKey_fromModulusAndExponent_decryptsRoundTrip() {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(1024)
        val keyPair = generator.generateKeyPair()
        val publicKey = keyPair.public as RSAPublicKey
        val referencePrivate = keyPair.private as RSAPrivateKey

        val modulus = toFixedLengthBytes(referencePrivate.modulus.toByteArray(), 128)
        val exponent = toFixedLengthBytes(referencePrivate.privateExponent.toByteArray(), 128)

        val rebuiltPrivateKey = SadadKeyCardCrypto.buildPrivateKey(modulus, exponent)

        val plaintext = "3DADD742625115A4C2FFEB0AD3AF8BA4".toByteArray().copyOf(16)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(plaintext)
        assertEquals(128, encrypted.size)

        val decrypted = SadadKeyCardCrypto.decryptTransportedKey(rebuiltPrivateKey, encrypted)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun kcvHex_doubleLengthKey_isThreeBytesHex() {
        val key = SadadHex.decode("3132333435363738393A3B3C3D3E3F20")
        val kcv = SadadKeyCardCrypto.kcvHex(key)
        assertEquals(6, kcv.length)
    }

    @Test
    fun kcvHex_sameKey_isDeterministic() {
        val key = SadadHex.decode("00112233445566778899AABBCCDDEEFF")
        assertEquals(SadadKeyCardCrypto.kcvHex(key), SadadKeyCardCrypto.kcvHex(key))
    }

    @Test
    fun kcvHex_differentKeys_produceDifferentValues() {
        val keyA = SadadHex.decode("00112233445566778899AABBCCDDEEFF")
        val keyB = SadadHex.decode("FFEEDDCCBBAA99887766554433221100")
        org.junit.Assert.assertNotEquals(
            SadadKeyCardCrypto.kcvHex(keyA),
            SadadKeyCardCrypto.kcvHex(keyB),
        )
    }

    private fun toFixedLengthBytes(signedBytes: ByteArray, length: Int): ByteArray {
        val unsigned = if (signedBytes.size > length && signedBytes[0] == 0.toByte()) {
            signedBytes.copyOfRange(1, signedBytes.size)
        } else {
            signedBytes
        }
        require(unsigned.size <= length) { "value too large for $length bytes" }
        val result = ByteArray(length)
        System.arraycopy(unsigned, 0, result, length - unsigned.size, unsigned.size)
        return result
    }
}
