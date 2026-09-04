package com.danesh.common.receipt

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

const val RESULT_AUTO_HOME_DELAY_MS = 10_000

@Composable
fun CountdownEffect(
    durationMillis: Int = RESULT_AUTO_HOME_DELAY_MS,
    onFinished: () -> Unit
) {
    LaunchedEffect(durationMillis) {
        val startTime = SystemClock.elapsedRealtime()
        while (true) {
            delay(500)
            val elapsed = SystemClock.elapsedRealtime() - startTime
            if (elapsed >= durationMillis) {
                onFinished()
                break
            }
        }
    }
}
