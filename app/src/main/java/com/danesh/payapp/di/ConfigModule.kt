package com.danesh.payapp.di

import com.danesh.payapp.BuildConfig
import com.danesh.payapp.config.ActiveDevice
import com.danesh.payapp.config.ActiveProtocol
import com.danesh.payapp.config.ActivePsp
import com.danesh.payapp.config.AppRuntimeConfig
import com.danesh.payapp.config.toReceiptPspBrand
import com.danesh.common.menu.MenuFlavorFeatures
import com.danesh.common.receipt.ReceiptPspBrandProvider
import com.danesh.payapp.config.toDefaultAppLanguage
import com.danesh.payapp.config.toDefaultMerchantPassword
import com.danesh.settings.config.DefaultMerchantPasswordProvider
import com.danesh.settings.config.MerchantPasswordLockPolicy
import com.danesh.payapp.config.toMerchantPasswordMaxFailedAttempts
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
    fun provideDefaultMerchantPasswordProvider(
        config: AppRuntimeConfig,
    ): DefaultMerchantPasswordProvider = DefaultMerchantPasswordProvider {
        config.activePsp.toDefaultMerchantPassword()
    }

    @Provides
    @Singleton
    fun provideMerchantPasswordLockPolicy(
        config: AppRuntimeConfig,
    ): MerchantPasswordLockPolicy = MerchantPasswordLockPolicy {
        config.activePsp.toMerchantPasswordMaxFailedAttempts()
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
