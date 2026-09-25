package com.danesh.common.card

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CardSessionEntryPoint {
    fun cardSession(): CardSession
}

/**
 * دکمه بک در میانه تراکنش (بعد از خواندن کارت): اطلاعات کارت از حافظه پاک و
 * کاربر مستقیم به صفحه اصلی برگردانده می‌شود — نه یک قدم عقب‌تر در همان فلو.
 */
@Composable
fun CardTransactionBackHandler(
    enabled: Boolean = true,
    onExitToHome: () -> Unit,
) {
    val context = LocalContext.current
    val cardSession = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            CardSessionEntryPoint::class.java,
        ).cardSession()
    }
    BackHandler(enabled = enabled) {
        cardSession.clear()
        onExitToHome()
    }
}
