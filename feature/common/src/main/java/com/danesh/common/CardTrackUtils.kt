package com.danesh.common

object CardTrackUtils {

    fun extractPan(track2: String): String {
        return normalizeTrack2(track2).substringBefore("=")
    }

    fun normalizeTrack2(raw: String): String {
        if (raw.contains('=') || raw.contains('^')) return raw
        if (raw.length % 2 != 0 || raw.length <= 37) return raw
        if (!raw.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }) return raw
        return buildString(raw.length / 2) {
            var i = 0
            while (i < raw.length - 1) {
                append(raw.substring(i, i + 2).toInt(16).toChar())
                i += 2
            }
        }
    }
}
