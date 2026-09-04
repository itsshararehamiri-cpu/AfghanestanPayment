package com.danesh.hp.key

/** کلیدهای کاری همراه‌پی — TMK و PIK/MAK رمزشده زیر TMK. */
object HpKeyMaterial {
    fun masterKeyBytes(): ByteArray = HpKeyConfig.MASTER_KEY_HEX.decodeHexKey()

    fun encryptedPinKeyBytes(): ByteArray = HpKeyConfig.ENCRYPTED_PIN_KEY_HEX.decodeHexKey()

    fun encryptedMacKeyBytes(): ByteArray = HpKeyConfig.ENCRYPTED_MAC_KEY_HEX.decodeHexKey()
}
