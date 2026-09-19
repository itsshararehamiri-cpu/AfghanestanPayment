package com.danesh.iso

/**
 * سداد هدر/TPDU مخصوص خودش را دارد: 0x60 + NII (۲ بایت) + 0x80 0xB5
 * (برخلاف BP که سافیکس آن 0x00 0x00 است) — ر.ک. BpWireFrame.buildTpdu.
 */
object SadadWireFrame {
    const val TPDU_BYTES = 5
    private const val HEADER_SUFFIX_HEX = "80B5"

    fun buildTpdu(nii: String): ByteArray {
        val niiNumber = nii.trim().toIntOrNull()
            ?: error("Sadad NII invalid: '$nii'")
        val niiHex = niiNumber.toString(16)
            .padStart(4, '0')
            .uppercase()
        val tpdu = ByteUtil.hex2byte("60$niiHex$HEADER_SUFFIX_HEX")
        require(tpdu.size == TPDU_BYTES) {
            "Sadad Header must be $TPDU_BYTES bytes, got ${tpdu.size}"
        }
        return tpdu
    }
}
