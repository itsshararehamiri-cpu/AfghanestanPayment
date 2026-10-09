package com.danesh.payapp.di

import com.danesh.core.DeviceSettings
import com.danesh.core.DeviceSettingsProvider
import com.danesh.core.PspDeviceSettingsOverride
import com.danesh.payapp.config.AppRuntimeConfig
import com.danesh.payapp.config.DeviceSettingsDefaults
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton

/**
 * تنظیمات سخت‌افزار (اندیس کلید، timeoutها، طول رمز، الگوریتم MAC) از اینجا به دستگاه داده می‌شود؛
 * خود پیاده‌سازی دستگاه (K9) مقدار پیش‌فرضی ندارد.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceSettingsModule {

    /** PSPها می‌توانند با `@IntoSet` بازنویسی بدهند؛ ممکن است خالی باشد. */
    @Multibinds
    abstract fun pspDeviceSettingsOverrides(): Set<PspDeviceSettingsOverride>

    companion object {
        @Provides
        @Singleton
        fun provideDeviceSettingsProvider(
            config: AppRuntimeConfig,
            overrides: Set<@JvmSuppressWildcards PspDeviceSettingsOverride>,
        ): DeviceSettingsProvider {
            val defaults = DeviceSettingsDefaults.forPsp(config.activePsp)
            return DeviceSettingsProvider {
                overrides.fold(defaults) { current, override -> current.apply(override) }
            }
        }

        private fun DeviceSettings.apply(override: PspDeviceSettingsOverride): DeviceSettings = copy(
            keyIndexes = runCatching { override.keyIndexes(keyIndexes) }.getOrNull() ?: keyIndexes,
            timeouts = runCatching { override.timeouts(timeouts) }.getOrNull() ?: timeouts,
            pinEntry = runCatching { override.pinEntry(pinEntry) }.getOrNull() ?: pinEntry,
        )
    }
}
