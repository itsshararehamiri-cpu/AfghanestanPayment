package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SupportServicesFlowViewModel @Inject constructor() : ViewModel() {
    var verifiedTrack2: String = ""
        private set
    var verifiedPan: String = ""
        private set

    fun onCardVerified(track2: String, pan: String) {
        verifiedTrack2 = track2
        verifiedPan = pan
    }

    fun clear() {
        verifiedTrack2 = ""
        verifiedPan = ""
    }
}
