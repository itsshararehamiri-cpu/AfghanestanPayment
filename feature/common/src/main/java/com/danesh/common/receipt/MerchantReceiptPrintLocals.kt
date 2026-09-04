package com.danesh.common.receipt

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

val LocalMerchantReceiptPrintMode = compositionLocalOf { MerchantReceiptPrintMode.OPTIONAL }

@Composable
fun rememberCurrentMerchantReceiptPrintMode(
    preferences: MerchantReceiptPrintPreferences,
): MerchantReceiptPrintMode {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mode by remember { mutableStateOf(preferences.getMode()) }
    DisposableEffect(lifecycleOwner, preferences) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mode = preferences.getMode()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return mode
}
