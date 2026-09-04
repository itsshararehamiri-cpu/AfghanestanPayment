package com.danesh.common.receipt.di

import com.danesh.api.QueueCustomerPrintMarker
import com.danesh.api.QueueCustomerReceiptPrinter
import com.danesh.api.SafQueueReader
import com.danesh.common.receipt.ComposeQueueCustomerReceiptPrinter
import com.danesh.common.receipt.DaoSafQueueReader
import com.danesh.common.receipt.DefaultQueueCustomerPrintMarker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QueueReceiptModule {

    @Binds
    @Singleton
    abstract fun bindSafQueueReader(
        impl: DaoSafQueueReader,
    ): SafQueueReader

    @Binds
    @Singleton
    abstract fun bindQueueCustomerReceiptPrinter(
        impl: ComposeQueueCustomerReceiptPrinter,
    ): QueueCustomerReceiptPrinter

    @Binds
    @Singleton
    abstract fun bindQueueCustomerPrintMarker(
        impl: DefaultQueueCustomerPrintMarker,
    ): QueueCustomerPrintMarker
}
