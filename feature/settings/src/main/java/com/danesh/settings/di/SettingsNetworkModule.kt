package com.danesh.settings.di

import com.danesh.settings.network.AndroidWifiNetworkScanner
import com.danesh.settings.network.WifiNetworkScanner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsNetworkModule {

    @Binds
    abstract fun bindWifiNetworkScanner(
        impl: AndroidWifiNetworkScanner,
    ): WifiNetworkScanner
}
