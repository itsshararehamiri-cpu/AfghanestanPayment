package com.danesh.sadad.di

import com.danesh.api.BillFlowPolicy
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.PspGateway
import com.danesh.api.QueueRemovalPolicy
import com.danesh.api.SafQueueFlusher
import com.danesh.api.SupportCatalog
import com.danesh.api.TransferFlowPolicy
import com.danesh.api.TransactionFeeCalculator
import com.danesh.engine.HostTimeSynchronizer
import com.danesh.engine.NoOpHostTimeSynchronizer
import com.danesh.engine.QueueAdviceExecutor
import com.danesh.engine.QueueProcessor
import com.danesh.engine.TransactionStore
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadGateway
import com.danesh.sadad.SadadTransactionStore
import com.danesh.sadad.bill.SadadBillFlowPolicy
import com.danesh.sadad.config.SadadConfigurationChecker
import com.danesh.sadad.config.SadadInitialConfigurationPolicy
import com.danesh.sadad.fee.ZeroTransactionFeeCalculator
import com.danesh.sadad.queue.SadadQueueAdviceExecutor
import com.danesh.sadad.queue.SadadQueueRemovalPolicy
import com.danesh.sadad.support.EmptySupportCatalog
import com.danesh.sadad.transfer.SadadTransferFlowPolicy
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
        impl: SadadTransactionStore,
    ): TransactionStore<IsoMessage>

    @Binds
    abstract fun bindQueueAdviceExecutor(
        impl: SadadQueueAdviceExecutor,
    ): QueueAdviceExecutor<IsoMessage>

    @Binds
    abstract fun bindQueueRemovalPolicy(
        impl: SadadQueueRemovalPolicy,
    ): QueueRemovalPolicy

    @Binds
    abstract fun bindPspGateway(
        impl: SadadGateway,
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
        impl: SadadConfigurationChecker,
    ): PspConfigurationChecker

    @Binds
    abstract fun bindBillFlowPolicy(
        impl: SadadBillFlowPolicy,
    ): BillFlowPolicy

    @Binds
    abstract fun bindTransferFlowPolicy(
        impl: SadadTransferFlowPolicy,
    ): TransferFlowPolicy

    @Binds
    abstract fun bindInitialConfigurationPolicy(
        impl: SadadInitialConfigurationPolicy,
    ): InitialConfigurationPolicy

    @Binds
    abstract fun bindTransactionFeeCalculator(
        impl: ZeroTransactionFeeCalculator,
    ): TransactionFeeCalculator

    companion object {
        @Provides
        @Singleton
        fun provideSafQueueFlusher(
            queueProcessor: QueueProcessor<IsoMessage>,
        ): SafQueueFlusher = SafQueueFlusher {
            queueProcessor.checkTxns(force = true)
        }
    }
}
