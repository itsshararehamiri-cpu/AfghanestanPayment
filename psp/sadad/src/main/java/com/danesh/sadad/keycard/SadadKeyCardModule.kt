package com.danesh.sadad.keycard

import com.danesh.api.KeyCardLoadingService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SadadKeyCardModule {

    @Binds
    @Singleton
    abstract fun bindIccTransport(impl: SadadDeviceIccTransport): SadadIccTransport

    @Binds
    @Singleton
    abstract fun bindKeyPairStore(impl: SadadKeyCardSecureStorage): SadadKeyCardKeyPairStore

    @Binds
    @Singleton
    abstract fun bindKeyCardLoadingService(
        impl: SadadKeyCardLoadingServiceAdapter,
    ): KeyCardLoadingService
}
