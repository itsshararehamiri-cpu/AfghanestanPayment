package com.danesh.engine.di

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.engine.DefaultTransactionSessionClock
import com.danesh.engine.SharedPrefsDeviceConfigurationStore
import com.danesh.engine.SharedPrefsTransactionContextProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionContextModule {

    @Binds
    @Singleton
    abstract fun bindTransactionContextProvider(
        impl: SharedPrefsTransactionContextProvider,
    ): TransactionContextProvider

    @Binds
    @Singleton
    abstract fun bindTransactionSessionClock(
        impl: DefaultTransactionSessionClock,
    ): TransactionSessionClock

    @Binds
    @Singleton
    abstract fun bindDeviceConfigurationStore(
        impl: SharedPrefsDeviceConfigurationStore,
    ): DeviceConfigurationStore
}
