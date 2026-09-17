package com.danesh.sadad.keycard

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
     *
     * رمزنگاری هم مثل کارت با RSA خام (بدون padding، فقط zero-pad تا 128 بایت) انجام
     * می‌شود، نه PKCS#1 v1.5 — دقیقاً همان چیزی که [decryptTransportedKey] اکنون می‌خواند.
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

        val plaintext = SadadHex.decode("3DADD742625115A4C2FFEB0AD3AF8BA4")
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(toFixedLengthBytes(plaintext, 128))
        assertEquals(128, encrypted.size)

        val decrypted = SadadKeyCardCrypto.decryptTransportedKey(rebuiltPrivateKey, encrypted)
        assertArrayEquals(plaintext, decrypted)
    }

    /**
     * سناریوی تست واقعی بخش ۴ مستند «راهنمای استفاده از کارت کلید» (F-P102, v2.0.1):
     * جفت کلید واقعی کارت A (اندیس ۰۱) و شش کلید رمزشده‌ی واقعی کارت C (اندیس ۰۱). این تست
     * تایید می‌کند [decryptTransportedKey] دقیقاً مقادیر رمزگشایی‌شده‌ی مستند را برمی‌گرداند
     * (کلید ۰۵ مستند ۳ کاراکتر هگز کسری دارد — ظاهراً خطای تایپی مستند — پس فقط با پیشوند آن
     * مطابقت داده می‌شود).
     */
    @Test
    fun decryptTransportedKey_documentedCardCTestVectors_matchDocumentedPlaintext() {
        val modulus = SadadHex.decode(
            "9BDDE541631E83F9DD83CF87A841AD1D3E75DFBEE4BCC899BD56886F3D979773C3D7B68DFAFEFB" +
                "2114AD7A4847A297ED110C38461362EE96B426924A7B19FC7AAEC378674ABF3F07AE8F52E080CA8" +
                "6E3B6945FBB9E30E44A26EDED18856DEA06674AA051487FE7BDA9551892FC7499E2BE640F81D204" +
                "507F8414DCD4154664AB",
        )
        val exponent = SadadHex.decode(
            "453CB471EF4DC6E25F7C05DC04E9E7B46DE27E99959226A9E4696401C62268AC8BFA6EA5733180E" +
                "C98FAD6AEF4E34D0989E11BBE452E553DAC98D5B3302BC2F32AE70308B8AE8E8281E1F52E97D7A9" +
                "863F8542AC8294E999AC6A9834F3326D7CB9345A978CAD6DBDAA3BF2FDF61658320687EF80C4BC7" +
                "5DDBBEAC0ED5FFE0841",
        )
        val privateKey = SadadKeyCardCrypto.buildPrivateKey(modulus, exponent)

        val vectors = listOf(
            "78544CD495AAD6C0D399B4ABC00AEBF86F2A88AD921C7C614E02792557FD981DFADCF662E857925" +
                "D3BA9A100079029B3F36A65B3E67C40FE2EAE33D6878D0312DCE3C2DB24B823ADFD93B2D9BC5FBB" +
                "D0E985ADE4065B97C6161C6C427704D3ECA16F2AA4951A6AB922DE87812228454A31BE9E9957793" +
                "97EF8B922353A871991" to "3DADD742625115A4C2FFEB0AD3AF8BA4",
            "19C71712A56268E05C32003C1A47C55BC5AA1BCA291208084C1DF06518972E237C6D331AE6D2DA1" +
                "04180FC3A4E97A381C581AD70550D76DBA452ADAA2D3FDE44E7D27BBD7FA296E4A92A95B4092423" +
                "0841E228728D7888C45931D9F0ACEEB6B55DBE39962789EDCA60C4B02F7F91BBC9EB25457EF67B3" +
                "3012FE08B929A87BB31" to "CFD1EE713792889B3F586EBF3BAF02BF",
            "9641841686BB28EAF575CBF5B607ADC4706E20B0E8BF18660474AE68025C95C7D4097ADC036923D" +
                "61EFC61AA3E436A22EA9725C49C123C071A1D3A14F3D258D3872D7BCDC0E48C2E5DAE340024043" +
                "0C86FE5894FD4B39A2C273A78793B69A10A21BCC6B3A504238A6D15C0E1F0D118CC253A6A7DE0A4" +
                "B9E9D362EDD8E57C435B" to "FE574E0370128B06C77F2819280561B8",
            "61B80E77B73E0A34F7F6FB5549C531A3F8F9234343375384FFCAF67DCB1F5DCED7AEE318BB7670C" +
                "5E69961E3E9507346C48594C47D1DFF36A4A7E360C3AEC81D80DF1756ACA99EA3A6BB75A164A272" +
                "9F8804C317931B3666A2E77AABE3EE8DE967A02B6517289AF45D900B619097620982C783E396A49" +
                "515D258A6F44CBD155C" to "A87B10872980E3B488949BA52BFBBA70",
            "1C517C819959A03935463B492E15195A0EB8FE2094647153548470F68D60985945FE32AFF2F850" +
                "1554306BA12ABA25E4639878D10039AC8C610DAF256D0D84E635C3A6E86BB2A94934F318C581BE" +
                "EC53B48689BAF6B6A6CC5E5926E7CB2FA7EA66319D3849433F46553E384EAD63224682F9D81553C" +
                "458FB47867991F7C69216" to "BEAABF1DB3E500695C909C94840A27BD",
            "798DDA534920357CFEBE3068F1264392C89FBE608EB8547414E30684CCC881E770DC97A862B6D5" +
                "80F21960FDFBB8AB5FB78AF712494760F674CFF0C8AAEC5B3F9A38F20806B4145B02C66C81B671" +
                "9BAACDFF24689C8A3E811AD0EF355DCCC8FA38E9C601D38DD278D3FA39727C35DD464250A8AF78A" +
                "65BABB65D727F8EE27FA9" to "E9C59983CA364C6B7C3B90F3F276D",
        )

        vectors.forEach { (encryptedHex, expectedPrefixHex) ->
            val decrypted = SadadKeyCardCrypto.decryptTransportedKey(privateKey, SadadHex.decode(encryptedHex))
            assertEquals(16, decrypted.size)
            assertTrue(SadadHex.encode(decrypted).startsWith(expectedPrefixHex))
        }
    }

    /**
     * اگر بایت اول کلید واقعی (۱۶ بایت انتهایی بلوک ۱۲۸ بایتی) خودش صفر باشد، حذف ساده‌ی
     * صفرهای ابتدایی (روش قبلی) اشتباهاً وارد کلید واقعی می‌شود و آن را کوتاه می‌کند. این تست
     * تایید می‌کند [decryptTransportedKey] با آفست ثابت ۱۱۲ (نه حذف صفر) این حالت را درست
     * مدیریت می‌کند، حتی اگر Cipher خروجی را با صفرهای ابتدایی حذف‌شده برگرداند.
     */
    @Test
    fun decryptTransportedKey_keyStartingWithZeroByte_isNotTruncated() {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(1024)
        val keyPair = generator.generateKeyPair()
        val publicKey = keyPair.public as RSAPublicKey
        val referencePrivate = keyPair.private as RSAPrivateKey

        val modulus = toFixedLengthBytes(referencePrivate.modulus.toByteArray(), 128)
        val exponent = toFixedLengthBytes(referencePrivate.privateExponent.toByteArray(), 128)
        val rebuiltPrivateKey = SadadKeyCardCrypto.buildPrivateKey(modulus, exponent)

        val plaintext = SadadHex.decode("00AA112233445566778899AABBCCDDEE")
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(toFixedLengthBytes(plaintext, 128))

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
