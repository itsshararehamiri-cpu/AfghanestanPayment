package com.danesh.afghanestanpayment.di

import com.danesh.afghanestanpayment.config.BuildConfigReceiptFeeDefaultsProvider
import com.danesh.api.ReceiptFeeDefaultsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReceiptFeeDefaultsModule {

    @Binds
    @Singleton
    abstract fun bindReceiptFeeDefaultsProvider(
        impl: BuildConfigReceiptFeeDefaultsProvider,
    ): ReceiptFeeDefaultsProvider
}
