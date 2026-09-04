package com.danesh.hp.di

import com.danesh.common.startup.AppStartupTask
import com.danesh.hp.KarenSignOnStartupTask
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object KarenStartupModule {

    @Provides
    @IntoSet
    fun provideKarenSignOnStartupTask(
        task: KarenSignOnStartupTask
    ): AppStartupTask = task
}