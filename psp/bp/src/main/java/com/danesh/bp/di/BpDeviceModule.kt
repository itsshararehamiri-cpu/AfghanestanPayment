package com.danesh.bp.di

import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.PspDeviceWorkflow
import com.danesh.bp.device.BpDeviceMetadataProvider
import com.danesh.bp.device.BpDeviceWorkflow
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BpDeviceModule {

    @Binds
    @Singleton
    abstract fun bindPspDeviceWorkflow(
        impl: BpDeviceWorkflow,
    ): PspDeviceWorkflow

    @Binds
    @Singleton
    abstract fun bindPspDeviceMetadataProvider(
        impl: BpDeviceMetadataProvider,
    ): PspDeviceMetadataProvider
}
