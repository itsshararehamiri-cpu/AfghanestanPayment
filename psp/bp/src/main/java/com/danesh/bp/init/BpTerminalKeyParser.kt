package com.danesh.bp.init

import com.danesh.bp.key.BpKeyParser

internal object BpTerminalKeyParser {

    fun parse(decrypted: ByteArray): ByteArray = BpKeyParser.parseKeyMaterial(decrypted)
}

internal fun ByteArray.wipe() {
    fill(0)
}
