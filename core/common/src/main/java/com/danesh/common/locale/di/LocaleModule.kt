package com.danesh.common.locale.di

import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.SharedPrefsLocalePreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocaleModule {
    @Binds
    @Singleton
    abstract fun bindLocalePreferences(
        impl: SharedPrefsLocalePreferences,
    ): LocalePreferences
}
