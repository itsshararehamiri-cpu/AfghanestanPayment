package com.danesh.hp.key

/**
 * ثابت‌های ISO مخصوص همراه‌پی (HP).
 *
 * در HP فیلد 24 Function Code است (نه NII شبکه) و برای هر تراکنش متفاوت است.
 * مقادیر Function Code در [com.danesh.api.TransactionIsoProfile.messageNii] تعریف شده‌اند.
 */
object HpKeyConfig {
    /** Terminal Master Key (TMK) — 16 بایت double-length DES. */
    const val MASTER_KEY_HEX = "0123456789ABCDEFFEDCBA9876543210"

    /** PIK رمزشده زیر TMK (16 بایت). */
    const val ENCRYPTED_PIN_KEY_HEX = "31A7364CAC91CA39C0489F69BEC54FA2"

    /** MAK رمزشده زیر TMK (16 بایت). */
    const val ENCRYPTED_MAC_KEY_HEX = "F7C6D4AFD6C3832AA16C07D1BA9F62DD"

    const val DEFAULT_TERMINAL_ID = "12345678"
    const val DEFAULT_MERCHANT_ID = "HPA000400300200"

    /**
     * مقدار پیش‌فرض عمومی merchantId در [com.danesh.api.TransactionContextProvider] (مشترک بین همه PSPها).
     * وقتی SharedPreferences هنوز برای همراه‌پی سفارشی نشده، خواندن از prefs همین مقدار را برمی‌گرداند؛
     * در آن حالت [applyHpAcceptorIds][com.danesh.hp.iso.HpIsoMessageSupport.applyHpAcceptorIds]
     * به‌جای آن از [DEFAULT_MERCHANT_ID] استفاده می‌کند.
     */
    const val ENGINE_DEFAULT_MERCHANT_ID = "44236789"

    /** درخواست مالی هسته — 1.1 */
    const val FINANCIAL_MTI = "1100"

    /** موجودی، استعلام قبض، استعلام نام — 1.1 */
    const val INQUIRY_MTI = "1600"

    /** Reversal advice — 1.1 */
    const val REVERSAL_MTI = "1420"

    /** DE22 — فقط کارت‌به‌کارت و کارت‌به‌کیف مالی */
    const val CARD_TO_CARD_POS_ENTRY_MODE = "100010100131"
    //100010100131

    /** DE26 — Card acceptor business code / MCC */
    const val MERCHANT_TYPE = "6018"

    /** فیلد 32 — Acquiring Institution ID */
    const val ACQUIRING_INSTITUTION_ID = "1307100"

    /** فیلد 51 — واحد پولی صورت‌حساب دارنده کارت */
    const val CARDHOLDER_BILLING_CURRENCY = "971"
    const val ASYCUDA="01000001"
}
