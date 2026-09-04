package com.danesh.database.di

import com.danesh.database.PayAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
  //  @Singleton
    fun provideTransactionReportDao(database: PayAppDatabase) = database.transactionReportDao()

    @Provides
  //  @Singleton
    fun provideStoreForwardQueueDao(database: PayAppDatabase) = database.storeForwardQueueDao()


}