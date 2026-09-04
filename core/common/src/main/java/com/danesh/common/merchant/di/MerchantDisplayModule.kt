package com.danesh.common.merchant.di

import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.merchant.SharedPrefsMerchantDisplayPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MerchantDisplayModule {
    @Binds
    @Singleton
    abstract fun bindMerchantDisplayPreferences(
        impl: SharedPrefsMerchantDisplayPreferences,
    ): MerchantDisplayPreferences
}
