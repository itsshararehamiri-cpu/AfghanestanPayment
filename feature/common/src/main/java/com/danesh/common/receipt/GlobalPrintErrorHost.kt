package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.os.Handler
import android.os.Looper
import com.danesh.core.PrintErrorNotifier
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PrintErrorNotifierEntryPoint {
    fun printErrorNotifier(): PrintErrorNotifier
}

/**
 * دیالوگ سراسری خطای پرینت — از [PrintErrorNotifier] که در [com.danesh.core.LoggingDevice] پر می‌شود.
 */
@Composable
fun GlobalPrintErrorHost() {
    val context = LocalContext.current
    val notifier = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PrintErrorNotifierEntryPoint::class.java,
        ).printErrorNotifier()
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(notifier) {
        val removeListener = notifier.addListener { message ->
            mainHandler.post { errorMessage = message }
        }
        onDispose { removeListener() }
    }

    errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
        PrintErrorDialog(
            message = message,
            onDismiss = { errorMessage = null },
        )
    }
}
