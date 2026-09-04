package com.danesh.common.network.di

import com.danesh.common.network.DefaultNetworkConnectivityMonitor
import com.danesh.common.network.DefaultSwitchConnectionChecker
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.SwitchConnectionChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityMonitor(
        impl: DefaultNetworkConnectivityMonitor,
    ): NetworkConnectivityMonitor

    @Binds
    @Singleton
    abstract fun bindSwitchConnectionChecker(
        impl: DefaultSwitchConnectionChecker,
    ): SwitchConnectionChecker
}
