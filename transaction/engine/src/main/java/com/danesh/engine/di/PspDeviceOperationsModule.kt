package com.danesh.engine.di

import com.danesh.api.PspDeviceOperations
import com.danesh.engine.DefaultPspDeviceOperations
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PspDeviceOperationsModule {

    @Binds
    @Singleton
    abstract fun bindPspDeviceOperations(
        impl: DefaultPspDeviceOperations,
    ): PspDeviceOperations
}
