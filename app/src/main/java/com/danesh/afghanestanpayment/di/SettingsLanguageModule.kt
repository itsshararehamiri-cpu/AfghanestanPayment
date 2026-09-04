package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.config.PspSettingsLanguageOptions
import com.danesh.settings.locale.SettingsLanguageOptions
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsLanguageModule {

    @Binds
    @Singleton
    abstract fun bindSettingsLanguageOptions(
        impl: PspSettingsLanguageOptions,
    ): SettingsLanguageOptions
}
