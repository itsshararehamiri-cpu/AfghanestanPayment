package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.afghanestanpayment.config.ActiveDevice
import com.danesh.afghanestanpayment.config.ActiveProtocol
import com.danesh.afghanestanpayment.config.ActivePsp
import com.danesh.afghanestanpayment.config.AppRuntimeConfig
import com.danesh.afghanestanpayment.config.toReceiptPspBrand
import com.danesh.common.menu.MenuFlavorFeatures
import com.danesh.common.receipt.ReceiptPspBrandProvider
import com.danesh.afghanestanpayment.config.toDefaultAppLanguage
import com.danesh.common.locale.DefaultAppLanguageProvider
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptCalendarStyleProvider
import com.danesh.common.locale.toReceiptCalendarStyle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideMenuFlavorFeatures(config: AppRuntimeConfig): MenuFlavorFeatures =
        MenuFlavorFeatures { config.enabledFeatures }

    @Provides
    @Singleton
    fun provideReceiptPspBrandProvider(config: AppRuntimeConfig): ReceiptPspBrandProvider =
        ReceiptPspBrandProvider {
            config.activePsp.toReceiptPspBrand()
        }

    @Provides
    @Singleton
    fun provideDefaultAppLanguageProvider(
        config: AppRuntimeConfig,
    ): DefaultAppLanguageProvider = DefaultAppLanguageProvider {
        config.activePsp.toDefaultAppLanguage()
    }

    @Provides
    @Singleton
    fun provideReceiptCalendarStyleProvider(
        localePreferences: LocalePreferences,
    ): ReceiptCalendarStyleProvider =
        ReceiptCalendarStyleProvider {
            localePreferences.getLanguage().toReceiptCalendarStyle()
        }

    @Provides
    @Singleton
    fun provideAppRuntimeConfig(): AppRuntimeConfig {
        return AppRuntimeConfig(
            activePsp = ActivePsp.valueOf(BuildConfig.ACTIVE_PSP),
            activeProtocol = ActiveProtocol.valueOf(BuildConfig.ACTIVE_PROTOCOL),
            activeDevice = ActiveDevice.valueOf(BuildConfig.ACTIVE_DEVICE),
            enabledFeatures = BuildConfig.ENABLED_FEATURES
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet(),
        )
    }
}
