package com.danesh.payapp.di

import com.danesh.payapp.config.BuildConfigPspPlatformLabelProvider
import com.danesh.common.psp.PspPlatformLabelProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PspPlatformModule {
    @Binds
    @Singleton
    abstract fun bindPspPlatformLabelProvider(
        impl: BuildConfigPspPlatformLabelProvider,
    ): PspPlatformLabelProvider
}
