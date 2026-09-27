package com.danesh.payapp.di

import com.danesh.payapp.connection.BuildConfigIsoMessageCreator
import com.danesh.payapp.connection.BuildConfigIsoPackagerProvider
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
