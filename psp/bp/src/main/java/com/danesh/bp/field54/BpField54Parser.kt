package com.danesh.bp.field54


object BpField54Parser {

    const val ITEM_LENGTH = 16
    private const val CURRENCY_LENGTH = 3
    private const val INDICATOR_LENGTH = 1
    private const val AMOUNT_LENGTH = 12
    private const val MAX_ITEMS = 2

    fun parse(raw: String?): BpField54Balances? {
        val value = raw?.trim().orEmpty()
        if (value.length < ITEM_LENGTH) return null

        val itemCount = (value.length / ITEM_LENGTH).coerceAtMost(MAX_ITEMS)
        if (itemCount < 1) return null

        val items = (0 until itemCount).mapNotNull { index ->
            parseItem(value.substring(index * ITEM_LENGTH, (index + 1) * ITEM_LENGTH))
        }
        if (items.isEmpty()) return null

        return BpField54Balances(
            actual = items[0],
            available = items.getOrNull(1),
        )
    }

    private fun parseItem(chunk: String): BpField54BalanceItem? {
        if (chunk.length != ITEM_LENGTH) return null
        val currency = chunk.substring(0, CURRENCY_LENGTH)
        val indicator = chunk[CURRENCY_LENGTH]
        val amount = chunk.substring(
            CURRENCY_LENGTH + INDICATOR_LENGTH,
            CURRENCY_LENGTH + INDICATOR_LENGTH + AMOUNT_LENGTH,
        )
        if (!currency.all { it.isDigit() }) return null
        if (indicator != 'C' && indicator != 'D') return null
        if (!amount.all { it.isDigit() }) return null
        return BpField54BalanceItem(
            currencyCode = currency,
            debitCredit = indicator,
            amount = amount,
        )
    }
}

data class BpField54Balances(
    val actual: BpField54BalanceItem,
    val available: BpField54BalanceItem? = null,
)

data class BpField54BalanceItem(
    val currencyCode: String,
    val debitCredit: Char,
    val amount: String,
) {
    fun toDisplayAmount(): String {
        val normalized = amount.trimStart('0').ifEmpty { "0" }
        return if (debitCredit == 'D' && normalized != "0") "-$normalized" else normalized
    }
}
