package com.danesh.api

data class BalanceInput(
    val track2: String,
    val pinBlock: String,
    val pan: String
)
data class SignOnInput(
    val id: String,

)
data class TerminalConfigInput(
    val id: String,

    )
data class BillInput(
    val track2: String,
    val pinBlock: String,
    val pan: String,
    val billId: String,
    val payId: String,
    val amount: String = "0",
    /** تگ 898 پاسخ استعلام — فقط برای همراه‌پی (Payment Save) */
    val requestId: String = "",
)

data class BillInquiryInput(
    val billId: String,
    val payId: String,
    val track2: String = "",
    val pan: String = "",
)

//data class BillInquiryOutput(
//    val isSuccess: Boolean,
//    val responseCode: String = "",
//    val responseMessage: String = "",
//    val billId: String = "",
//    val billAmount: String = "",
//    val companyCode: String = "",
//    val payerName: String = "",
//    /** تگ 898 برای لینک Info → Save */
//    val requestId: String = "",
//    val remainingBalance: String = "",
//)

typealias BalanceOutput = TransactionResultDetail

typealias BillOutput = TransactionResultDetail

typealias BillInquiryOutput= TransactionResultDetail
data class PurchaseInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0, val pan: String
)

typealias PurchaseOutput = TransactionResultDetail

data class VoucherInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long ,
    val pan: String ,
    val operatorCode: String,
)
data class TopUpInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0,
    val pan: String = "",
    val mobileNumber: String,
    val operatorCode: String,
)

typealias VoucherOutput = TransactionResultDetail
typealias TopUpOutput = TransactionResultDetail


data class CashDepositInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0,val destinationAccount: String, val pan: String
)

typealias CashDepositOutput = TransactionResultDetail

data class CashOutInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0,val destinationAccount: String, val pan: String
)

typealias CashOutOutput = TransactionResultDetail

/** کارت‌به‌کارت همراه‌پی — مقصد در DE48 تگ 021 */
data class CardToCardInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0,
    val pan: String,
    val destinationPan: String,
    /** RRN استعلام نام قبلی (DE37) — در صورت خالی از تاریخ/زمان جلسه استفاده می‌شود */
    val rrn: String = "",
    /** نام دارنده مقصد از استعلام نام — DE48 تگ 049 */
    val holderName: String = "",
)

typealias CardToCardOutput = TransactionResultDetail

/** کارت‌به‌کیف‌پول همراه‌پی — مقصد در DE48 تگ 045 */
data class CardToWalletInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0,
    val pan: String,
    val walletCode: String,
    /** RRN استعلام نام قبلی (DE37) — در صورت خالی از تاریخ/زمان جلسه استفاده می‌شود */
    val rrn: String = "",
    /** نام دارنده مقصد از استعلام نام — DE48 تگ 049 */
    val holderName: String = "",
)

typealias CardToWalletOutput = TransactionResultDetail

data class WalletToWalletInput(
    val pinBlock: String,
    val amount: Long = 0,
    val sourceWallet: String,
    val destinationWallet: String,
    val rrn: String = "",
    val holderName: String = "",
)

typealias WalletToWalletOutput = TransactionResultDetail

data class NameInquiryInput(
    val forWallet: Boolean,
    val forWalletToWallet: Boolean = false,
    val destination: String,
    val pan: String = "",
    val sourceWallet: String = "",
    val track2: String = "",
)

data class NameInquiryOutput(
    val isSuccess: Boolean,
    val responseCode: String = "",
    val responseMessage: String = "",
    val holderName: String = "",
    /** DE37 پاسخ/درخواست استعلام — برای تراکنش مالی بعدی */
    val rrn: String = "",
    /** جزئیات کامل برای رسید در صورت خطا */
    val detail: TransactionResultDetail? = null,
)

data class SupportInput(
    val track2: String,
    val pinBlock: String,
    val amount: Long = 0,
    val pan: String = "",
    /** مقدار تگ ۲۴ فیلد ۴۸ — شناسهٔ آیتم پشتیبانی انتخاب‌شده */
    val serviceId: String,
)

typealias SupportOutput = TransactionResultDetail

sealed interface TransactionInput
sealed interface TransactionOutput

enum class TransactionType {
    BALANCE,
    PURCHASE,
    CARD_TO_CARD,
    BILL,
    ADVICE,
    SETTLEMENT,
    CASH_DEPOSIT,
    CASH_OUT,
    LOGON,
    INIT,
    VOUCHER,
    TOPUP,
    /** تراکنش پشتیبانی به‌پرداخت (processing code 100000) */
    SUPPORT,
    CARD_TO_WALLET,
    WALLET_TO_WALLET,
    SIGNON,TERMINAL_CONFIG
}

typealias LogonOutput = TransactionResultDetail
typealias InitOutput = TransactionResultDetail
typealias SignOnOutput = TransactionResultDetail
typealias TerminalConfigOutput = TransactionResultDetail



interface PspGateway {
    suspend fun bill(input: BillInput): BillOutput

    suspend fun billInquiry(input: BillInquiryInput): BillInquiryOutput

    suspend fun balance(input: BalanceInput): BalanceOutput
    suspend fun purchase(input: PurchaseInput): PurchaseOutput
    suspend fun voucher(input: VoucherInput): VoucherOutput
    suspend fun topUp(input: TopUpInput): TopUpOutput

    suspend fun cashDeposit(input: CashDepositInput): CashDepositOutput
    suspend fun cashOut(input: CashOutInput): CashOutOutput
    suspend fun cardToCard(input: CardToCardInput): CardToCardOutput
    suspend fun cardToWallet(input: CardToWalletInput): CardToWalletOutput
    suspend fun walletToWallet(input: WalletToWalletInput): WalletToWalletOutput
    /** استعلام نام دارنده مقصد قبل از تایید انتقال (FC 651) — همراه‌پی */
    suspend fun nameInquiry(input: NameInquiryInput): NameInquiryOutput
    suspend fun support(input: SupportInput): SupportOutput
    suspend fun logon(masterKey: String): LogonOutput
    suspend fun init(input: InitInput): InitOutput
    suspend fun signOn(input:SignOnInput): SignOnOutput
    suspend fun terminalConfig(input:TerminalConfigInput): TerminalConfigOutput

}
