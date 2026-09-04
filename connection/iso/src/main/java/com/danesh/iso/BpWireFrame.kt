package com.danesh.iso


object BpWireFrame {
    const val LENGTH_BYTES = 2
    const val TPDU_BYTES = 5

    data class ParsedPayload(
        val tpdu: ByteArray,
        val isoBody: ByteArray,
    )
    fun buildTpdu(nii: String): ByteArray {
        val niiNumber = nii.trim().toIntOrNull()
            ?: error("BP NII invalid: '$nii'")
        val niiHex = niiNumber.toString(16)
            .padStart(4, '0')
            .uppercase()
        val tpdu = ByteUtil.hex2byte("60${niiHex}0000")
        require(tpdu.size == TPDU_BYTES) {
            "BP Header must be $TPDU_BYTES bytes, got ${tpdu.size}"
        }
        return tpdu
    }

    fun requireTpdu(header: ByteArray?): ByteArray {
        require(header != null && header.size == TPDU_BYTES) {
            "BP Header/TPDU must be exactly $TPDU_BYTES bytes, got ${header?.size ?: 0}"
        }
        return header
    }

    fun buildFrame(tpdu: ByteArray, isoBody: ByteArray): ByteArray {
        val header = requireTpdu(tpdu)
        val payloadLength = TPDU_BYTES + isoBody.size
        val frame = ByteArray(LENGTH_BYTES + payloadLength)
        val lengthBytes = encodeLength(payloadLength)
        System.arraycopy(lengthBytes, 0, frame, 0, LENGTH_BYTES)
        System.arraycopy(header, 0, frame, LENGTH_BYTES, TPDU_BYTES)
        System.arraycopy(isoBody, 0, frame, LENGTH_BYTES + TPDU_BYTES, isoBody.size)
        return frame
    }

    fun parsePayloadAfterLength(payload: ByteArray): ParsedPayload {
        require(payload.size >= TPDU_BYTES) {
            "BP payload too short: ${payload.size} bytes, need at least $TPDU_BYTES for Header"
        }
        return ParsedPayload(
            tpdu = payload.copyOfRange(0, TPDU_BYTES),
            isoBody = payload.copyOfRange(TPDU_BYTES, payload.size),
        )
    }

    fun encodeLength(payloadLength: Int): ByteArray {
        require(payloadLength in 0..0xFFFF) {
            "BP payload length out of range: $payloadLength"
        }
        return byteArrayOf(
            (payloadLength shr 8).toByte(),
            payloadLength.toByte(),
        )
    }

    fun decodeLength(lengthBytes: ByteArray): Int {
        require(lengthBytes.size == LENGTH_BYTES) {
            "BP length must be $LENGTH_BYTES bytes, got ${lengthBytes.size}"
        }
        return ((lengthBytes[0].toInt() and 0xFF) shl 8) or (lengthBytes[1].toInt() and 0xFF)
    }
}
