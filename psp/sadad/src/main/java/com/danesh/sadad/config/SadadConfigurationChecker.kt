package com.danesh.sadad.config

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.TransactionContextProvider
import com.danesh.sadad.key.SadadWorkingMacState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * سداد: دستگاه وقتی پیکربندی‌شده است که
 * ۱) INIT موفق بوده و شماره ترمینال، شماره پذیرنده و نام پذیرنده از آن ذخیره شده باشد، و
 * ۲) LOGON موفق بوده و کلیدهای کاری (PIN/MAC/DATA فیلد ۴۸) روی PED نشسته باشند.
 */
@Singleton
class SadadConfigurationChecker @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val configurationStore: DeviceConfigurationStore,
    private val workingMacState: SadadWorkingMacState,
) : PspConfigurationChecker {

    override fun isConfigured(): Boolean {
        val config = contextProvider.getTerminalConfig()
        val hasInitIdentity = config.terminalId.isNotBlank() &&
            config.merchantId.isNotBlank() &&
            config.merchantName.isNotBlank()
        return hasInitIdentity &&
            configurationStore.isConfigured() &&
            workingMacState.hasWorkingMac()
    }
}
