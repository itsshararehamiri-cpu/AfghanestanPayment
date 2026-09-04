package com.danesh.knine

import com.danesh.core.Device
import com.danesh.core.LoggingDevice
import com.danesh.core.PrintErrorNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceBindingModule {

    @Provides
    @Singleton
    fun providePrintErrorNotifier(): PrintErrorNotifier = PrintErrorNotifier()

    @Provides
    @Singleton
    fun provideDevice(k9: K9, printErrorNotifier: PrintErrorNotifier): Device =
        LoggingDevice(k9, printErrorNotifier)
}