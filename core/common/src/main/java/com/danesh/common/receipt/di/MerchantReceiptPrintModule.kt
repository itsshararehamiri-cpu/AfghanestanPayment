package com.danesh.common.receipt.di

import com.danesh.common.receipt.MerchantReceiptPrintPreferences
import com.danesh.common.receipt.SharedPrefsMerchantReceiptPrintPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MerchantReceiptPrintModule {
    @Binds
    @Singleton
    abstract fun bindMerchantReceiptPrintPreferences(
        impl: SharedPrefsMerchantReceiptPrintPreferences,
    ): MerchantReceiptPrintPreferences
}
