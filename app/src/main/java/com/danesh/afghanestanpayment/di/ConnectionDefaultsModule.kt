package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.connection.BuildConfigConnectionDefaultsProvider
import com.danesh.common.connection.ConnectionDefaultsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectionDefaultsModule {

    @Binds
    @Singleton
    abstract fun bindConnectionDefaultsProvider(
        impl: BuildConfigConnectionDefaultsProvider,
    ): ConnectionDefaultsProvider
}
