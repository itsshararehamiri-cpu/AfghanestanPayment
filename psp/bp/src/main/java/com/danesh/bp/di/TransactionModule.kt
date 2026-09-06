package com.danesh.bp.di

import android.util.Log
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.KeyCardLoadingService
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.PspGateway
import com.danesh.api.BillFlowPolicy
import com.danesh.api.SafQueueFlusher
import com.danesh.api.TransferFlowPolicy
import com.danesh.api.TransactionFeeCalculator
import com.danesh.bp.config.BpConfigurationChecker
import com.danesh.bp.config.BpInitialConfigurationPolicy
import com.danesh.api.QueueRemovalPolicy
import com.danesh.api.SupportCatalog
import com.danesh.bp.bill.BpBillFlowPolicy
import com.danesh.bp.BpGateway
import com.danesh.bp.BpTransactionStore
import com.danesh.bp.fee.BpTransactionFeeCalculator
import com.danesh.bp.queue.BpQueueAdviceExecutor
import com.danesh.bp.queue.BpQueueRemovalPolicy
import com.danesh.bp.support.BpSupportMenuStore
import com.danesh.bp.time.BpHostTimeSynchronizer
import com.danesh.bp.transfer.BpTransferFlowPolicy
import com.danesh.engine.HostTimeSynchronizer
import com.danesh.engine.QueueAdviceExecutor
import com.danesh.engine.QueueProcessor
import com.danesh.engine.TransactionStore
import com.danesh.iso.IsoMessage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionModule {

    @Binds
    abstract fun bindTransactionStore(
        impl: BpTransactionStore,
    ): TransactionStore<IsoMessage>

    @Binds
    abstract fun bindQueueAdviceExecutor(
        impl: BpQueueAdviceExecutor,
    ): QueueAdviceExecutor<IsoMessage>

    @Binds
    abstract fun bindQueueRemovalPolicy(
        impl: BpQueueRemovalPolicy,
    ): QueueRemovalPolicy

    @Binds
    abstract fun bindPspGateway(
        impl: BpGateway,
    ): PspGateway

    @Binds
    abstract fun bindSupportCatalog(
        impl: BpSupportMenuStore,
    ): SupportCatalog

    @Binds
    abstract fun bindSupportMenuPersister(
        impl: BpSupportMenuStore,
    ): com.danesh.bp.support.SupportMenuResponsePersister

    @Binds
    abstract fun bindHostTimeSynchronizer(
        impl: BpHostTimeSynchronizer,
    ): HostTimeSynchronizer

    @Binds
    abstract fun bindPspConfigurationChecker(
        impl: BpConfigurationChecker,
    ): PspConfigurationChecker

    @Binds
    abstract fun bindBillFlowPolicy(
        impl: BpBillFlowPolicy,
    ): BillFlowPolicy

    @Binds
    abstract fun bindTransferFlowPolicy(
        impl: BpTransferFlowPolicy,
    ): TransferFlowPolicy

    @Binds
    abstract fun bindInitialConfigurationPolicy(
        impl: BpInitialConfigurationPolicy,
    ): InitialConfigurationPolicy

    companion object {
        @Provides
        @Singleton
        fun provideKeyCardLoadingService(): KeyCardLoadingService =
            com.danesh.api.UnsupportedKeyCardLoadingService

        @Provides
        @Singleton
        fun provideTransactionFeeCalculator(
            impl: BpTransactionFeeCalculator,
        ): TransactionFeeCalculator = impl

        @Provides
        @Singleton
        fun provideSafQueueFlusher(
            queueProcessor: QueueProcessor<IsoMessage>,
        ): SafQueueFlusher = SafQueueFlusher {
            queueProcessor.checkTxns(force = true)
        }
    }
}
