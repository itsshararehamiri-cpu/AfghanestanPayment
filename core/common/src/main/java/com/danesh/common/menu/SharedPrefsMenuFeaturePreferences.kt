package com.danesh.common.menu

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SharedPrefsMenuFeaturePreferences @Inject constructor(
    @ApplicationContext context: Context,
) : MenuFeaturePreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _disabledFeatures = MutableStateFlow(readDisabled())
    override val disabledFeatures: StateFlow<Set<String>> = _disabledFeatures.asStateFlow()

    override fun isFeatureEnabled(featureName: String): Boolean =
        featureName !in readDisabled()

    override fun setFeatureEnabled(featureName: String, enabled: Boolean) {
        val updated = readDisabled().toMutableSet()
        if (enabled) {
            updated.remove(featureName)
        } else {
            updated.add(featureName)
        }
        prefs.edit().putStringSet(KEY_DISABLED, updated).apply()
        _disabledFeatures.value = updated.toSet()
    }

    override fun resetToDefaults() {
        prefs.edit().remove(KEY_DISABLED).apply()
        _disabledFeatures.value = emptySet()
    }

    private fun readDisabled(): Set<String> =
        prefs.getStringSet(KEY_DISABLED, emptySet())?.toSet().orEmpty()

    companion object {
        private const val PREFS_NAME = "menu_feature_prefs"
        private const val KEY_DISABLED = "disabled_menu_features"
    }
}
