package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.core.Device
import com.danesh.settings.domain.toInitialConfigurationSummary
import com.danesh.settings.model.InitialConfigurationSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import javax.inject.Inject

@HiltViewModel
class ConfigurationSummaryViewModel @Inject constructor(
    private val configurationStore: DeviceConfigurationStore,
    private val device: Device,
    private val contextProvider: TransactionContextProvider,
    private val appVersionProvider: AppVersionProvider,
    private val localePreferences: LocalePreferences,
) : ViewModel() {

    fun resolveSummaryForDisplay(): InitialConfigurationSummary? {
        configurationStore.getConfigurationSummary()
            ?.toInitialConfigurationSummary()
            ?.let { return it }

        if (!configurationStore.isConfigured()) return null

        val terminalConfig = contextProvider.getTerminalConfig()
        val programDate = ReceiptNowFormatter.format(localePreferences)
        return InitialConfigurationSummary(
            hardwareSerial = runCatching { device.getSerial() }.getOrElse { "" }.ifBlank { "-" },
            terminalId = terminalConfig.terminalId.ifBlank { "-" },
            appVersion = appVersionProvider.versionName().ifBlank { "-" },
            programDate = programDate,
            merchantId = terminalConfig.merchantId.ifBlank { "-" },
        )
    }
}
