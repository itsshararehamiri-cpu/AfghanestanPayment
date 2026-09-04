package com.danesh.iso.di

import com.danesh.core.ConnectionConfig
import com.danesh.iso.ConnectionConfigImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    fun provideConnectionConfig(): ConnectionConfig {
        return ConnectionConfigImp(
            // values
        )
    }
}