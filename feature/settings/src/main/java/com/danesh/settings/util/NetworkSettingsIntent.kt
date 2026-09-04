package com.danesh.settings.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

fun openNetworkSettings(context: Context) {
    val intents = listOf(
        Intent(Settings.ACTION_WIFI_SETTINGS),
        Intent(Settings.ACTION_WIRELESS_SETTINGS),
        Intent(Settings.ACTION_SETTINGS),
    )

    for (intent in intents) {
        runCatching {
            context.startActivity(
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return
        }
    }
}
