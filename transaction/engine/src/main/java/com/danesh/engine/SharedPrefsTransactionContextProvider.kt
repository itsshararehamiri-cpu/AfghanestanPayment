package com.danesh.engine

import android.content.Context
import android.util.Log
import com.danesh.api.CurrencyDefaultsProvider
import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionContextProvider
import com.danesh.api.VatPercentageDefaults
import com.danesh.api.VatPercentageRules
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsTransactionContextProvider @Inject constructor(
    @ApplicationContext context: Context,
    private val currencyDefaults: CurrencyDefaultsProvider,
) : TransactionContextProvider {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val stanLock = Any()

    override fun getTerminalConfig(): TerminalConfig {
        val v= TerminalConfig(
            terminalId = prefs.getString(KEY_TERMINAL_ID, DEFAULT_TERMINAL_ID).orEmpty(),
            merchantId = prefs.getString(KEY_MERCHANT_ID, DEFAULT_MERCHANT_ID).orEmpty(),
            merchantName = prefs.getString(KEY_MERCHANT_NAME, DEFAULT_MERCHANT_NAME).orEmpty(),
            merchantPhone = prefs.getString(KEY_MERCHANT_PHONE, DEFAULT_MERCHANT_PHONE).orEmpty(),
            englishMerchantName = prefs.getString(KEY_ENGLISH_MERCHANT_NAME, DEFAULT_ENGLISH_MERCHANT_NAME).orEmpty(),
            merchantAddress = prefs.getString(KEY_MERCHANT_ADDRESS, DEFAULT_MERCHANT_ADDRESS).orEmpty(),
            merchantPostalCode = prefs.getString(KEY_MERCHANT_POSTAL_CODE, DEFAULT_MERCHANT_POSTAL_CODE).orEmpty(),
            nii = prefs.getString(KEY_NII, DEFAULT_NII).orEmpty(),
            pointOfServiceEntryMode = prefs.getString(KEY_POS_ENTRY_MODE, DEFAULT_POS_ENTRY_MODE).orEmpty(),
            // واحد پولی از PSP فعال می‌آید (BP=364، HP=592)، نه از مقدار قدیمی prefs
            currency = currencyDefaults.currencyCode,
            functionCode = prefs.getString(KEY_FUNCTION_CODE, DEFAULT_FUNCTION_CODE).orEmpty(),
            deviceSerial = prefs.getString(KEY_DEVICE_SERIAL, DEFAULT_DEVICE_SERIAL).orEmpty(),
            mcc = prefs.getString(KEY_MCC, DEFAULT_MCC).orEmpty(),
            configPayload = prefs.getString(KEY_CONFIG_PAYLOAD, DEFAULT_CONFIG_PAYLOAD).orEmpty(),
        )
        return v
    }

    override fun saveTerminalConfig(config: TerminalConfig) {
        prefs.edit()
            .putString(KEY_TERMINAL_ID, config.terminalId)
            .putString(KEY_MERCHANT_ID, config.merchantId)
            .putString(KEY_MERCHANT_NAME, config.merchantName)
            .putString(KEY_MERCHANT_PHONE, config.merchantPhone)
            .putString(KEY_ENGLISH_MERCHANT_NAME, config.englishMerchantName)
            .putString(KEY_MERCHANT_ADDRESS, config.merchantAddress)
            .putString(KEY_MERCHANT_POSTAL_CODE, config.merchantPostalCode)
            .putString(KEY_NII, config.nii)
            .putString(KEY_POS_ENTRY_MODE, config.pointOfServiceEntryMode)
            .putString(KEY_CURRENCY, config.currency)
            .putString(KEY_FUNCTION_CODE, config.functionCode)
            .putString(KEY_DEVICE_SERIAL, config.deviceSerial)
            .putString(KEY_MCC, config.mcc)
            .putString(KEY_CONFIG_PAYLOAD, config.configPayload)
            .apply()
    }

    override fun nextStan(): String {
        synchronized(stanLock) {
            val next = prefs.getInt(KEY_STAN, STAN_INITIAL_VALUE) + 1
            prefs.edit().putInt(KEY_STAN, next).apply()
            return next.toString().padStart(STAN_LENGTH, '0')
        }
    }

    override fun currentClock(): TransactionClock {
        val now = Date()
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)
        val time = SimpleDateFormat("HHmmss", Locale.US).format(now)
        return TransactionClock(date = date, time = time)
    }

    override fun saveVatPercentage(varPercentage: String) {
        prefs.edit()
            .putString(VAT_PERCENTAGE, varPercentage)

            .apply()
    }

    override fun getVatPercentage(): String =
        VatPercentageRules.resolve(prefs.getString(VAT_PERCENTAGE, null))

    override fun lastSuccessfulStan(): String =
        prefs.getString(KEY_LAST_SUCCESS_STAN, DEFAULT_LAST_SUCCESS_STAN).orEmpty()
            .ifBlank { DEFAULT_LAST_SUCCESS_STAN }

    override fun lastSuccessfulRrn(): String =
        prefs.getString(KEY_LAST_SUCCESS_RRN, DEFAULT_LAST_SUCCESS_RRN).orEmpty()
            .ifBlank { DEFAULT_LAST_SUCCESS_RRN }

    override fun saveLastSuccessfulTransaction(stan: String, rrn: String?) {
        val normalizedStan = stan.filter(Char::isDigit).padStart(STAN_LENGTH, '0').takeLast(STAN_LENGTH)
        val normalizedRrn = rrn?.filter(Char::isDigit)?.padStart(RRN_LENGTH, '0')?.takeLast(RRN_LENGTH)
        prefs.edit()
            .putString(KEY_LAST_SUCCESS_STAN, normalizedStan.ifBlank { DEFAULT_LAST_SUCCESS_STAN })
            .putString(
                KEY_LAST_SUCCESS_RRN,
                normalizedRrn?.takeIf { it.isNotBlank() } ?: DEFAULT_LAST_SUCCESS_RRN,
            )
            .apply()
    }

    override fun clearTerminalData() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "terminal_transaction_prefs"
        private const val KEY_TERMINAL_ID = "terminal_id"
        private const val KEY_MERCHANT_ID = "merchant_id"
        private const val KEY_MERCHANT_NAME = "merchant_name"
        private const val KEY_MERCHANT_PHONE = "merchant_phone"
        private const val KEY_ENGLISH_MERCHANT_NAME = "english_merchant_name"
        private const val KEY_MERCHANT_ADDRESS = "merchant_address"
        private const val KEY_MERCHANT_POSTAL_CODE = "merchant_postal_code"

        private const val KEY_NII = "nii"
        private const val KEY_POS_ENTRY_MODE = "pos_entry_mode"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_FUNCTION_CODE = "function_code"
        private const val KEY_DEVICE_SERIAL = "device_serial"
        private const val KEY_MCC = "mcc"
        private const val KEY_CONFIG_PAYLOAD = "hp_config_payload"
        private const val KEY_STAN = "stan_counter"
        private const val KEY_LAST_SUCCESS_STAN = "last_success_stan"
        private const val KEY_LAST_SUCCESS_RRN = "last_success_rrn"
        const val VAT_PERCENTAGE = "vat_percentage"
        const val DEFAULT_VAT_PERCENT = VatPercentageDefaults.DEFAULT_PERCENT

        private const val STAN_LENGTH = 6
        private const val RRN_LENGTH = 12
        private const val STAN_INITIAL_VALUE = 0
        const val DEFAULT_LAST_SUCCESS_STAN = "000000"
        const val DEFAULT_LAST_SUCCESS_RRN = "000000000000"

        private const val DEFAULT_TERMINAL_ID = "12345678"//
        private const val DEFAULT_MERCHANT_ID = "44236789"//

        // TODO:
        private const val DEFAULT_MERCHANT_NAME = "تست"//تست
        private const val DEFAULT_MERCHANT_PHONE = "0214232733"//
        private const val DEFAULT_ENGLISH_MERCHANT_NAME = "Test"//
        private const val DEFAULT_MERCHANT_ADDRESS = "هرات"//
        private const val DEFAULT_MERCHANT_POSTAL_CODE = "1234567890"//

        private const val DEFAULT_NII = ""
        private const val DEFAULT_POS_ENTRY_MODE = ""
        private const val DEFAULT_FUNCTION_CODE = ""
        private const val DEFAULT_DEVICE_SERIAL = ""
        private const val DEFAULT_MCC = ""
        private const val DEFAULT_CONFIG_PAYLOAD = ""


    }
}
