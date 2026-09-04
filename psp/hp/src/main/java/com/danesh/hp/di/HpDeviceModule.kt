package com.danesh.hp.di

import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.PspDeviceWorkflow
import com.danesh.hp.device.HpDeviceMetadataProvider
import com.danesh.hp.device.HpDeviceWorkflow
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HpDeviceModule {

    @Binds
    @Singleton
    abstract fun bindPspDeviceWorkflow(
        impl: HpDeviceWorkflow,
    ): PspDeviceWorkflow

    @Binds
    @Singleton
    abstract fun bindPspDeviceMetadataProvider(
        impl: HpDeviceMetadataProvider,
    ): PspDeviceMetadataProvider
}
