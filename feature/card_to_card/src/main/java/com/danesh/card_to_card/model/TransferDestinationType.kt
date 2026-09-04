package com.danesh.card_to_card.model

enum class TransferDestinationType {
    CARD,
    WALLET;


    companion object {
        fun fromNav(value: String): TransferDestinationType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CARD
    }
}
