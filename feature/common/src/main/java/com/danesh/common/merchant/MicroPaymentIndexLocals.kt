package com.danesh.common.merchant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

val LocalMicroPaymentIndexConfig = compositionLocalOf { MicroPaymentIndexConfig.Default }

@Composable
fun rememberMicroPaymentIndexConfig(
    preferences: MerchantDisplayPreferences,
): MicroPaymentIndexConfig {
    val lifecycleOwner = LocalLifecycleOwner.current
    var config by remember { mutableStateOf(MicroPaymentIndexConfig.from(preferences)) }
    DisposableEffect(lifecycleOwner, preferences) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                config = MicroPaymentIndexConfig.from(preferences)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return config
}
