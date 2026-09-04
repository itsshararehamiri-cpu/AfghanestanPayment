package com.danesh.iso.di

import com.danesh.core.Connection
import com.danesh.iso.IsoMessage
import com.danesh.iso.JposConnectionProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectionModule {
    @Binds
    @Singleton
    abstract fun bindConnection(
        provider: JposConnectionProvider,
    ): Connection<IsoMessage>
}
