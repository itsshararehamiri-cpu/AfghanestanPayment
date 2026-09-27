package com.danesh.payapp.di

import com.danesh.payapp.config.BuildConfigCurrencyDefaultsProvider
import com.danesh.api.CurrencyDefaultsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyDefaultsModule {

    @Binds
    @Singleton
    abstract fun bindCurrencyDefaultsProvider(
        impl: BuildConfigCurrencyDefaultsProvider,
    ): CurrencyDefaultsProvider
}
