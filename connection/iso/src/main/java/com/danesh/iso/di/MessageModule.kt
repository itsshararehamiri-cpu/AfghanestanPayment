package com.danesh.iso.di

import com.danesh.common.RawMessage
import com.danesh.iso.IsoMessageProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MessageModule {
    @Provides
    fun provideRawMessage(
        messageProvider: IsoMessageProvider,
    ): RawMessage = messageProvider.create()
}
