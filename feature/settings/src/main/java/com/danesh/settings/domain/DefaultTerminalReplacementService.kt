package com.danesh.settings.domain

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.SupportCatalog
import com.danesh.api.TerminalReplacementHook
import com.danesh.api.TerminalReplacementService
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.common.card.CardSession
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.menu.MenuFeaturePreferences
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.receipt.MerchantReceiptPrintPreferences
import com.danesh.core.Device
import com.danesh.database.dao.StoreForwardQueueDao
import com.danesh.database.dao.TransactionReportDao
import com.danesh.settings.data.SettingsPasswordRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DefaultTerminalReplacementService @Inject constructor(
    private val reportDao: TransactionReportDao,
    private val queueDao: StoreForwardQueueDao,
    private val contextProvider: TransactionContextProvider,
    private val deviceConfigurationStore: DeviceConfigurationStore,
    private val connectionPreferences: ConnectionPreferences,
    private val menuFeaturePreferences: MenuFeaturePreferences,
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
    private val merchantReceiptPrintPreferences: MerchantReceiptPrintPreferences,
    private val localePreferences: LocalePreferences,
    private val passwordRepository: SettingsPasswordRepository,
    private val sessionClock: TransactionSessionClock,
    private val supportCatalog: SupportCatalog,
    private val device: Device,
    private val cardSession: CardSession,
    private val replacementHooks: Set<@JvmSuppressWildcards TerminalReplacementHook>,
) : TerminalReplacementService {

    override suspend fun replaceTerminal() = withContext(Dispatchers.IO) {
        reportDao.clearTable()
        queueDao.deleteAll()
        contextProvider.clearTerminalData()
        deviceConfigurationStore.clearConfigured()
        deviceConfigurationStore.clearConfigurationSummary()
        connectionPreferences.resetToDefaults()
        menuFeaturePreferences.resetToDefaults()
        merchantDisplayPreferences.resetToDefaults()
        merchantReceiptPrintPreferences.resetToDefaults()
        localePreferences.resetToDefaults()
        passwordRepository.resetAccessSettings()
        sessionClock.clear()
        supportCatalog.clear()
        device.clearMasterKeyCache()
        device.clearWorkingMacKeyCache()
        cardSession.clear()
        replacementHooks.forEach { hook -> hook.onTerminalReplaced() }
    }
}
