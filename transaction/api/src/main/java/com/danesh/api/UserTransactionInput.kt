package com.danesh.api

/**
 * ورودی‌هایی که در زمان انجام تراکنش از کاربر (کارت/پین) گرفته می‌شوند.
 * سایر فیلدها از [TerminalConfig] و ثابت‌های [TransactionIsoProfile] تأمین می‌شوند.
 */
sealed interface UserTransactionInput : TransactionRequest {
    val pinBlock: String
    val track2: String
    val pan: String
}

data class PurchaseUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val amount: String,
) : UserTransactionInput


data class SignOnUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
) : UserTransactionInput
data class TerminalConfigUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
) : UserTransactionInput

data class BalanceUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,

    ) : UserTransactionInput

data class BillUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val billId: String,
    val payId: String,
    val amount: String = "0",
    val requestId: String = "",
) : UserTransactionInput

/** درخواست استعلام قبض همراه‌پی (با PAN؛ بدون پین و بدون DE35). */
data class BillInquiryRequest(
    val billId: String,
    val payId: String,
    val track2: String = "",
    val pan: String = "",
) : TransactionRequest

data class CashDepositUserInput(
    override val pinBlock: String,
    override val track2: String,    override val pan: String,

    val amount: String,    val destinationAccount: String

) : UserTransactionInput

data class CashOutUserInput(
    override val pinBlock: String,
    override val track2: String,    override val pan: String,

    val amount: String,    val destinationAccount: String

) : UserTransactionInput

data class VoucherUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val amount: String,
    val operatorCode: String,
) : UserTransactionInput

data class TopUpUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val amount: String,
    val mobileNumber: String,
    val operatorCode: String,
) : UserTransactionInput

data class SupportUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val amount: String,
    /** تگ ۲۴ فیلد ۴۸ */
    val serviceId: String,
) : UserTransactionInput


/** کارت‌به‌کارت همراه‌پی — destinationPan دقیقاً ۱۶ رقم (DE48 تگ 021) */
data class CardToCardUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val amount: String,
    val destinationPan: String,
    val rrn: String = "",
    val holderName: String = "",
) : UserTransactionInput

/** کارت‌به‌کیف‌پول همراه‌پی — walletCode دقیقاً ۸ رقم (DE48 تگ 045) */
data class CardToWalletUserInput(
    override val pinBlock: String,
    override val track2: String,
    override val pan: String,
    val amount: String,
    val walletCode: String,
    val rrn: String = "",
    val holderName: String = "",
) : UserTransactionInput

data class WalletToWalletUserInput(
    override val pinBlock: String,
    override val track2: String = "",
    override val pan: String,
    val amount: String,
    val destinationWallet: String,
    val rrn: String = "",
    val holderName: String = "",
) : UserTransactionInput

data class NameInquiryRequest(
    val forWallet: Boolean,
    val forWalletToWallet: Boolean = false,
    val destination: String,
    val pan: String = "",
    val sourceWallet: String = "",
    val track2: String = "",
) : TransactionRequest
