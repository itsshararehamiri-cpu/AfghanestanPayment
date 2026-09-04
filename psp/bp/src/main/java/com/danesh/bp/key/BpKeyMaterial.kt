package com.danesh.bp.key

object BpKeyMaterial {
    fun initialMasterKeyBytes(): ByteArray = BpKeyConfig.INITIAL_MASTER_KEY_HEX.decodeHexKey()

    fun initialMacKeyBytes(): ByteArray = BpKeyConfig.INITIAL_MAC_KEY_HEX.decodeHexKey()

    fun initialKeyBytes(): ByteArray = initialMasterKeyBytes()
}
