package com.danesh.payapp.di

import com.danesh.payapp.config.PspSettingsMenuVisibility
import com.danesh.settings.config.SettingsMenuVisibilityProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsMenuVisibilityModule {

    @Binds
    @Singleton
    abstract fun bindSettingsMenuVisibilityProvider(
        impl: PspSettingsMenuVisibility,
    ): SettingsMenuVisibilityProvider
}
