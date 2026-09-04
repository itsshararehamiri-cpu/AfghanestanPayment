package com.danesh.common.card

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardSession @Inject constructor() {
    var track2: String = ""
        private set

    var pan: String = ""
        private set

    fun set(track2: String, pan: String) {
        this.track2 = track2
        this.pan = pan
    }

    fun clear() {
        track2 = ""
        pan = ""
    }
}
