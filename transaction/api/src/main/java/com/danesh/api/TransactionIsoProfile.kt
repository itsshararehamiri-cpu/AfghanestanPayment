package com.danesh.api

/**
 * مقادیر ثابت ISO برای هر نوع تراکنش (MTI، processing code، MAC و ...).
 */
enum class TransactionIsoProfile(
    val mti: String,
    val processingCode: String,
    /**
     * همراه‌پی: Function Code فیلد 24 (مثلاً 702 موجودی، 774 خرید، 511 استعلام قبض).
     * به‌پرداخت / پیام‌های شبکه: در صورت null از [TerminalConfig.nii] استفاده می‌شود.
     */
    val messageNii: String? = null,
) {
    PURCHASE(
        mti = "1100",
        processingCode = "000000",
        messageNii = "774",
    ),
    BALANCE(
        mti = "1100",
        processingCode = "310000",
        messageNii = "702",
    ),
    /*
    PURCHASE(
        mti = "0200",
        processingCode = "000000",
    ),
    BALANCE(
        mti = "0200",
        processingCode = "310000",
    ),
     */
    CASH_DEPOSIT(
        mti = "1100",
        processingCode = "210000",
        messageNii = "702",
    ),
    CASH_OUT(
        mti = "1100",
        processingCode = "010000",
        messageNii = "702",
    ),
    /** Bill Payment — Info (Function Code 511) */
    BILL_INQUIRY(
        mti = "1100",
        processingCode = "310000",
        messageNii = "511",
    ),
    /** Bill Payment — Save (Function Code 508) */
    BILL_PAYMENT(
        mti = "1100",
        processingCode = "500000",
        messageNii = "508",
    ),
    /** Card-to-Card Transfer — Function Code 689 */
    CARD_TO_CARD(
        mti = "1100",
        processingCode = "500000",
        messageNii = "689",
    ),
    /** Card-to-Wallet Transfer — Function Code 781 */
    CARD_TO_WALLET(
        mti = "1100",
        processingCode = "500000",
        messageNii = "781",
    ),
    WALLET_TO_WALLET(
        mti = "1100",
        processingCode = "400000",
        messageNii = "785",
    ),
    /** Cardholder / Wallet Holder Name Inquiry — Function Code 651 */
    NAME_INQUIRY(
        mti = "1600",
        processingCode = "350000",
        messageNii = "651",
    ),
    LOGON(
        mti = "0800",
        processingCode = "920000",
    ),
    INIT(
        mti = "1304",
        processingCode = "900000",
    ),
    SUPPORT(
        mti = "0200",
        processingCode = "100000",
    ),
    SIGNON(mti = "1804", processingCode = "", messageNii = "801"),
    TERMINAL_CONFIG(mti = "1304", processingCode = "", messageNii = "305");


    val emptyMac: ByteArray
        get() = EMPTY_MAC

    fun resolveAmount(input: UserTransactionInput): String = when (input) {
        is BalanceUserInput -> "0"
        is PurchaseUserInput -> input.amount
        is CashDepositUserInput -> input.amount
        is CashOutUserInput -> input.amount
        is CardToCardUserInput -> input.amount
        is CardToWalletUserInput -> input.amount
        is WalletToWalletUserInput -> input.amount
        is VoucherUserInput -> input.amount
        is SupportUserInput -> input.amount
        is TopUpUserInput -> input.amount
        is BillUserInput -> input.amount.ifBlank { "0" }
        is SignOnUserInput ->""
        is TerminalConfigUserInput -> ""
    }

    companion object {
        private val EMPTY_MAC = ByteArray(8)
    }
}
