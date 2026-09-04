package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.connection.BuildConfigIsoMessageCreator
import com.danesh.afghanestanpayment.connection.BuildConfigIsoPackagerProvider
import com.danesh.iso.IsoMessageCreator
import com.danesh.iso.IsoPackagerProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IsoPackagerModule {

    @Binds
    @Singleton
    abstract fun bindIsoPackagerProvider(
        impl: BuildConfigIsoPackagerProvider,
    ): IsoPackagerProvider

    @Binds
    @Singleton
    abstract fun bindIsoMessageCreator(
        impl: BuildConfigIsoMessageCreator,
    ): IsoMessageCreator
}
