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
