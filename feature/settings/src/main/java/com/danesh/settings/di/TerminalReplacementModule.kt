package com.danesh.settings.di

import com.danesh.api.TerminalReplacementHook
import com.danesh.api.TerminalReplacementService
import com.danesh.settings.domain.DefaultTerminalReplacementService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TerminalReplacementModule {

    @Binds
    @Singleton
    abstract fun bindTerminalReplacementService(
        impl: DefaultTerminalReplacementService,
    ): TerminalReplacementService

    /**
     * اجازه می‌دهد Set حتی بدون هوک flavor (مثل HP) خالی باشد.
     * BP همچنان با [@IntoSet] هوک خود را اضافه می‌کند.
     */
    @Multibinds
    abstract fun bindTerminalReplacementHooks(): Set<@JvmSuppressWildcards TerminalReplacementHook>
}
