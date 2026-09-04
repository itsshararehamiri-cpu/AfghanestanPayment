package com.danesh.bp.di

import com.danesh.api.TerminalReplacementHook
import com.danesh.bp.init.BpInitRsaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object BpTerminalReplacementModule {

    @Provides
    @IntoSet
    fun provideBpInitRsaSessionClearHook(
        rsaSession: BpInitRsaSession,
    ): TerminalReplacementHook = TerminalReplacementHook {
        rsaSession.clear()
    }
}
