package com.danesh.bp.field22


object BpPosEntryMode {

    enum class CardEntry(val code: String) {
        MANUAL("01"),
        MAGNETIC("02"),
        CHIP("05"),
        NFC("07"),
        QR("79"),
        SMART_VIA_MAGNETIC("80"),
    }

    enum class PinCapability(val code: String) {
        CAPABLE("1"),
        NOT_CAPABLE("2"),
    }

    fun compose(
        cardEntry: CardEntry,
        pinCapability: PinCapability = PinCapability.CAPABLE,
    ): String = cardEntry.code + pinCapability.code


    fun forCurrentCardRead(): String = compose(CardEntry.MAGNETIC, PinCapability.CAPABLE)
}
