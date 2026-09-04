package com.danesh.common.connection.di

import com.danesh.common.connection.ConnectionPreferences
import com.danesh.common.connection.SharedPrefsConnectionPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectionPreferencesModule {
    @Binds
    @Singleton
    abstract fun bindConnectionPreferences(
        impl: SharedPrefsConnectionPreferences,
    ): ConnectionPreferences
}
