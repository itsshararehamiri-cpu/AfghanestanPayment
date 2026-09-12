package com.danesh.api

import android.R
import com.google.gson.annotations.SerializedName

data class TransactionResultDetail(

    @SerializedName("isSuccess")
    val isSuccess: Boolean = false,

    @SerializedName("transactionType")
    val transactionType: TransactionType = TransactionType.BALANCE,

    @SerializedName("terminalId")
    val terminalId: String = "",

    @SerializedName("merchantId")
    val merchantId: String = "",

    @SerializedName("merchantName")
    val merchantName: String = "",

    @SerializedName("pan")
    val pan: String = "",

    @SerializedName("maskedPan")
    val maskedPan: String = "",

    @SerializedName("trace")
    val trace: String = "",

    @SerializedName("rrn")
    val rrn: String? = null,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("time")
    val time: String = "",

    @SerializedName("dateTime")
    val dateTime: String = "",

    @SerializedName("responseCode")
    val responseCode: String = "",

    @SerializedName("responseMessage")
    val responseMessage: String = "",

    @SerializedName("amount")
    val amount: String = "",

    /**
     * موجودی واقعی (Actual) — معمولاً از فیلد 54 آیتم ۱
     */
    @SerializedName("actualBalance")
    val actualBalance: String? = null,

    /**
     * موجودی قابل برداشت (Available) — معمولاً از فیلد 54 آیتم ۲ یا تگ ۴۸
     */
    @SerializedName("availableBalance")
    val availableBalance: String? = null,

    /**
     * متن میزبان از فیلد 55 به‌پرداخت (ANS …999).
     * اگر پر باشد، اول روی رسید الکترونیکی/کاغذی نمایش/چاپ می‌شود.
     */
    @SerializedName("hostReceiptText")
    val hostReceiptText: String? = null,

    /**
     * متن میزبان از فیلد 56 به‌پرداخت (ANS …999).
     * اگر پر باشد، دوم (بعد از hostReceiptText) روی رسید نمایش/چاپ می‌شود.
     */
    @SerializedName("hostReceiptTextSecond")
    val hostReceiptTextSecond: String? = null,

    @SerializedName("merchantPhone")
    val merchantPhone: String = "",

    @SerializedName("englishMerchantName")
    val englishMerchantName: String = "",

    @SerializedName("merchantAddress")
    val merchantAddress: String = "",

    @SerializedName("merchantPostalCode")
    val merchantPostalCode: String = "",

    @SerializedName("issuerName")
    val issuerName: String = "",

    @SerializedName("posCode")
    val posCode: String = "",

    @SerializedName("masterkey")
    val masterkey: String = "",

    @SerializedName("voucherSerial")
    val voucherSerial: String = "",

    @SerializedName("voucherPin")
    val voucherPin: String = "",

    @SerializedName("productCode")
    val productCode: String = "",

    @SerializedName("mobileNumber")
    val mobileNumber: String = "",

    @SerializedName("billId")
    val billId: String = "",

    @SerializedName("payId")
    val payId: String = "",

    /**
     * شماره کارت مقصد — کارت به کارت
     */
    @SerializedName("destinationPan")
    val destinationPan: String = "",

    /**
     * کد کیف پول مقصد — کارت به کیف پول
     */
    @SerializedName("walletCode")
    val walletCode: String = "",

    /**
     * نام گیرنده
     */
    @SerializedName("" +
            "")
    val holderName: String = "",

    /**
     * زمان دریافت گزارش — فقط برای چاپ مجدد رسید
     */
    @SerializedName("reprintReportDateTime")
    val reprintReportDateTime: String? = null,

    @SerializedName("voucherMethod")
    val voucherMethod: String? = null,

    /**
     * تگ 030 پیکربندی فعال (DE72 پاسخ 1314 همراه‌پی) — طبق رفتار رسید، بعد از لوگو چاپ می‌شود.
     */
    @SerializedName("receiptHeaderText")
    val receiptHeaderText: String = "",

    /**
     * تگ 031 پیکربندی فعال (DE72 پاسخ 1314 همراه‌پی) — طبق رفتار رسید، در انتهای رسید چاپ می‌شود.
     */
    @SerializedName("receiptFooterText")
    val receiptFooterText: String = "",
)

fun Int.toTransactionType(): TransactionType = when (this) {
    0 -> TransactionType.BALANCE
    1 -> TransactionType.PURCHASE
    2 -> TransactionType.CARD_TO_CARD
    3 -> TransactionType.BILL
    4 -> TransactionType.ADVICE
    5 -> TransactionType.SETTLEMENT
    6 -> TransactionType.CASH_DEPOSIT
    7 -> TransactionType.CASH_OUT
    8 -> TransactionType.LOGON
    9 -> TransactionType.INIT
    10 -> TransactionType.VOUCHER
    11 -> TransactionType.SUPPORT
    12 -> TransactionType.SUPPORT
    13 -> TransactionType.CARD_TO_WALLET
    14 -> TransactionType.WALLET_TO_WALLET
    else -> TransactionType.BALANCE
}
