package com.danesh.common.menu.di

import com.danesh.common.menu.MenuFeaturePreferences
import com.danesh.common.menu.SharedPrefsMenuFeaturePreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MenuFeatureModule {
    @Binds
    @Singleton
    abstract fun bindMenuFeaturePreferences(
        impl: SharedPrefsMenuFeaturePreferences,
    ): MenuFeaturePreferences
}
