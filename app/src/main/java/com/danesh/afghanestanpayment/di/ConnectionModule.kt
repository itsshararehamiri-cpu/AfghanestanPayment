package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.connection.BuildConfigJposConnectionProvider
import com.danesh.iso.JposConnectionProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppConnectionModule {

    @Binds
    @Singleton
    abstract fun bindJposConnectionProvider(
        impl: BuildConfigJposConnectionProvider,
    ): JposConnectionProvider
}
