package com.danesh.common.menu

import kotlinx.coroutines.flow.StateFlow

interface MenuFeaturePreferences {
    val disabledFeatures: StateFlow<Set<String>>

    fun isFeatureEnabled(featureName: String): Boolean

    fun setFeatureEnabled(featureName: String, enabled: Boolean)

    fun resetToDefaults()
}
