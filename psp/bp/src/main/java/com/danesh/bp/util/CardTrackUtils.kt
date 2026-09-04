package com.danesh.bp.util

object CardTrackUtils {

    fun extractPan(track2: String): String {
        if (track2.isBlank()) return ""
        val separatorIndex = track2.indexOfFirst { it == '=' || it == 'D' || it == 'd' }
        val panPart = if (separatorIndex >= 0) {
            track2.substring(0, separatorIndex)
        } else {
            track2
        }
        return panPart.filter(Char::isDigit)
    }
}
