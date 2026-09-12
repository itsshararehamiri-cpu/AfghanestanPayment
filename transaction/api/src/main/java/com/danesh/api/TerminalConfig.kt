package com.danesh.api

data class TerminalConfig(
    val terminalId: String,
    val merchantId: String,
    /** نام فارسی پذیرنده — تگ 09 فیلد 48 */
    val merchantName: String,
    val merchantPhone: String,
    val nii: String,
    val pointOfServiceEntryMode: String,
    val currency: String,
    val functionCode: String = "",
    val deviceSerial: String = "",
    /** نام انگلیسی پذیرنده — تگ 0C فیلد 48 */
    val englishMerchantName: String = "",
    /** آدرس پذیرنده — تگ 0B فیلد 48 */
    val merchantAddress: String = "",
    /** کد پستی پذیرنده — تگ 0D فیلد 48 */
    val merchantPostalCode: String = "",
    /**
     * کد گروه پذیرنده (MCC) — DE26. برای همراه‌پی از پاسخ 1314 پیکربندی ترمینال
     * (بخش 9.3 مستند KAREN) دریافت و در سایر تراکنش‌های مالی استفاده می‌شود؛ تا قبل
     * از اولین پیکربندی موفق خالی است و [com.danesh.hp.key.HpKeyConfig.MERCHANT_TYPE]
     * به‌عنوان مقدار پیش‌فرض به‌کار می‌رود.
     */
    val mcc: String = "",
    /**
     * فیلد 72 خامِ پاسخ 1314 پیکربندی ترمینال همراه‌پی (رکوردهای TLV `TTTLLLVALUE`)،
     * بدون تغییر ذخیره می‌شود. بخش 9.3 مستند KAREN تگ‌های 001، 002، 009، 010، 012،
     * 020، 021، 022، 023، 030، 031، 040، 041، 050، 051، 052، 060، 070 را در این payload
     * مجاز می‌داند (011 = هش تأیید که در [com.danesh.hp.config.TerminalConfigHandler]
     * اعتبارسنجی و مصرف می‌شود). خواندن هر تگ لازم — مثلاً 030/receipt_header و
     * 031/receipt_footer طبق بخش 12 مستند — با
     * `com.danesh.iso.field48.HpField48Tlv().apply { unpack(configPayload) }.getNode(tag)`
     * در محل مصرف انجام شود؛ تا قبل از اولین پیکربندی موفق خالی است.
     */
    val configPayload: String = "",
)

data class TransactionClock(
    val date: String,
    val time: String,
)

interface TransactionContextProvider {
    fun getTerminalConfig(): TerminalConfig
    fun saveTerminalConfig(config: TerminalConfig)
    fun nextStan(): String
    fun currentClock(): TransactionClock
    fun saveVatPercentage(varPercentage: String)
    fun getVatPercentage(): String
    /** STAN (فیلد 11) آخرین تراکنش مالی موفق — برای تگ 04 فیلد 48 */
    fun lastSuccessfulStan(): String
    /** RRN (فیلد 37) آخرین تراکنش مالی موفق — برای تگ 05 فیلد 48 */
    fun lastSuccessfulRrn(): String
    fun saveLastSuccessfulTransaction(stan: String, rrn: String?)
    fun clearTerminalData()
}
