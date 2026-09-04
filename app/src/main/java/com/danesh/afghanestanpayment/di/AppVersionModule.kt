package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.app.BuildConfigAppVersionProvider
import com.danesh.common.app.AppVersionProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppVersionModule {

    @Binds
    @Singleton
    abstract fun bindAppVersionProvider(
        impl: BuildConfigAppVersionProvider,
    ): AppVersionProvider
}
