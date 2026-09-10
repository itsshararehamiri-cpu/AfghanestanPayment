package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.api.InitialConfigurationPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    initialConfigurationPolicy: InitialConfigurationPolicy,
) : ViewModel() {
    val usesTerminalConfigFlow: Boolean = initialConfigurationPolicy.usesTerminalConfigFlow
}
