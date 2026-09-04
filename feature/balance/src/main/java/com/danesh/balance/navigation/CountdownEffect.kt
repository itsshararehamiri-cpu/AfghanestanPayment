package com.danesh.balance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun CountdownEffect(
    durationMillis: Int,
    onFinished: () -> Unit
) {
    var remainingTime by remember { mutableIntStateOf(durationMillis) }

    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime -= 1000
            if (remainingTime <= 0) {
                onFinished()
            }
        }
    }
}
