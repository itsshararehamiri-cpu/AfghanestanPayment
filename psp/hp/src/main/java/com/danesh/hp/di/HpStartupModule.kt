package com.danesh.hp.di

import com.danesh.common.startup.AppStartupTask
import com.danesh.engine.SafQueueScheduler
import com.danesh.iso.IsoMessage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object HpStartupModule {

    @Provides
    @IntoSet
    fun provideSafQueueScheduler(
        scheduler: SafQueueScheduler<IsoMessage>,
    ): AppStartupTask = scheduler
}
