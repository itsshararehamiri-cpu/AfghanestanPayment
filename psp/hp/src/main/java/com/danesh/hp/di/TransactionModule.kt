package com.danesh.hp.di

import com.danesh.api.BillFlowPolicy
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.KeyCardLoadingService
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.PspGateway
import com.danesh.api.QueueRemovalPolicy
import com.danesh.api.SafQueueFlusher
import com.danesh.api.SupportCatalog
import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransferFlowPolicy
import com.danesh.api.UnsupportedKeyCardLoadingService
import com.danesh.engine.HostTimeSynchronizer
import com.danesh.engine.NoOpHostTimeSynchronizer
import com.danesh.engine.QueueAdviceExecutor
import com.danesh.engine.QueueProcessor
import com.danesh.engine.TransactionStore
import com.danesh.hp.HpGateway
import com.danesh.hp.HpTransactionStore
import com.danesh.hp.bill.HpBillFlowPolicy
import com.danesh.hp.config.HpConfigurationChecker
import com.danesh.hp.config.HpInitialConfigurationPolicy
import com.danesh.hp.fee.ZeroTransactionFeeCalculator
import com.danesh.hp.queue.HpQueueAdviceExecutor
import com.danesh.hp.queue.HpQueueRemovalPolicy
import com.danesh.hp.support.EmptySupportCatalog
import com.danesh.hp.transfer.HpTransferFlowPolicy
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
        impl: HpTransactionStore,
    ): TransactionStore<IsoMessage>

    @Binds
    abstract fun bindQueueAdviceExecutor(
        impl: HpQueueAdviceExecutor,
    ): QueueAdviceExecutor<IsoMessage>

    @Binds
    abstract fun bindQueueRemovalPolicy(
        impl: HpQueueRemovalPolicy,
    ): QueueRemovalPolicy

    @Binds
    abstract fun bindPspGateway(
        impl: HpGateway,
    ): PspGateway

    @Binds
    abstract fun bindSupportCatalog(
        impl: EmptySupportCatalog,
    ): SupportCatalog

    @Binds
    abstract fun bindHostTimeSynchronizer(
        impl: NoOpHostTimeSynchronizer,
    ): HostTimeSynchronizer

    @Binds
    abstract fun bindPspConfigurationChecker(
        impl: HpConfigurationChecker,
    ): PspConfigurationChecker

    @Binds
    abstract fun bindBillFlowPolicy(
        impl: HpBillFlowPolicy,
    ): BillFlowPolicy

    @Binds
    abstract fun bindTransferFlowPolicy(
        impl: HpTransferFlowPolicy,
    ): TransferFlowPolicy

    @Binds
    abstract fun bindInitialConfigurationPolicy(
        impl: HpInitialConfigurationPolicy,
    ): InitialConfigurationPolicy

    @Binds
    abstract fun bindTransactionFeeCalculator(
        impl: ZeroTransactionFeeCalculator,
    ): TransactionFeeCalculator

    companion object {
        @Provides
        @Singleton
        fun provideKeyCardLoadingService(): KeyCardLoadingService =
            UnsupportedKeyCardLoadingService

        @Provides
        @Singleton
        fun provideSafQueueFlusher(
            queueProcessor: QueueProcessor<IsoMessage>,
        ): SafQueueFlusher = SafQueueFlusher {
            queueProcessor.checkTxns(force = true)
        }
    }
}
