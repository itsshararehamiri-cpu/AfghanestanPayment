package com.danesh.sadad.key

object SadadKeyMaterial {
    fun masterKeyBytes(): ByteArray = SadadKeyConfig.MASTER_KEY_HEX.decodeHexKey()

    fun encryptedPinKeyBytes(): ByteArray = SadadKeyConfig.ENCRYPTED_PIN_KEY_HEX.decodeHexKey()

    fun encryptedMacKeyBytes(): ByteArray = SadadKeyConfig.ENCRYPTED_MAC_KEY_HEX.decodeHexKey()
}
