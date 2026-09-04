package com.danesh.sadad.di

import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.PspDeviceWorkflow
import com.danesh.sadad.device.SadadDeviceMetadataProvider
import com.danesh.sadad.device.SadadDeviceWorkflow
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SadadDeviceModule {

    @Binds
    @Singleton
    abstract fun bindPspDeviceWorkflow(
        impl: SadadDeviceWorkflow,
    ): PspDeviceWorkflow

    @Binds
    @Singleton
    abstract fun bindPspDeviceMetadataProvider(
        impl: SadadDeviceMetadataProvider,
    ): PspDeviceMetadataProvider
}
