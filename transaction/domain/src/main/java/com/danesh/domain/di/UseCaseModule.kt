//package com.danesh.domain.di
//
////class UseCaseModule {
////}
//import com.danesh.domain.BalanceTransactionUseCase
//import com.danesh.domain.BalanceTransactionUseCaseImpl
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class UseCaseModule {
//        @Binds
//    @Singleton
//    abstract fun bindBalanceTransactionUseCase(k9: BalanceTransactionUseCaseImpl): BalanceTransactionUseCase
////    @Provides
////    @Singleton
////    fun provideBalanceTransactionUseCase(
////        factory: PspTransactionFactory<IsoMessage>,
////        orchestrator: TransactionOrchestrator<IsoMessage>
////    ): BalanceTransactionUseCase =
////        BalanceTransactionUseCaseImpl(orchestrator,factory)
//
////    @Provides
////    @Singleton
////    fun provideDevice(
////
////    ): Device = K9(ApplicationContext::class.java as Context)
//}