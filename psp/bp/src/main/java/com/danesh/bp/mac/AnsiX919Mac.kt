package com.danesh.bp.mac

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


object AnsiX919Mac {

    enum class Padding {
        ZERO,
        ISO9797_METHOD2,
    }

    fun calculate(
        key: ByteArray,
        data: ByteArray,
        padding: Padding = Padding.ZERO,
    ): ByteArray {
        require(key.size == 16 || key.size == 24) {
            "X9.19 key must be 16 or 24 bytes, was ${key.size}"
        }
        val k1 = key.copyOfRange(0, 8)
        val k2 = key.copyOfRange(8, 16)
        val k3 = if (key.size >= 24) key.copyOfRange(16, 24) else key.copyOfRange(0, 8)
        val padded = when (padding) {
            Padding.ZERO -> zeroPad(data)
            Padding.ISO9797_METHOD2 -> method2Pad(data)
        }
        var state = ByteArray(8)
        var offset = 0
        while (offset < padded.size) {
            val block = padded.copyOfRange(offset, offset + 8)
            state = desEcb(k1, xor(state, block), encrypt = true)
            offset += 8
        }
        state = desEcb(k2, state, encrypt = false)
        return desEcb(k3, state, encrypt = true)
    }

    private fun zeroPad(data: ByteArray): ByteArray {
        val mod = data.size % 8
        if (mod == 0) return data
        return data.copyOf(data.size + (8 - mod))
    }

    private fun method2Pad(data: ByteArray): ByteArray {
        val withMarker = data + 0x80.toByte()
        return zeroPad(withMarker)
    }

    private fun xor(a: ByteArray, b: ByteArray): ByteArray =
        ByteArray(8) { index -> (a[index].toInt() xor b[index].toInt()).toByte() }

    private fun desEcb(key8: ByteArray, block: ByteArray, encrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance("DES/ECB/NoPadding")
        val mode = if (encrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE
        cipher.init(mode, SecretKeySpec(key8, "DES"))
        return cipher.doFinal(block)
    }
}
