package com.danesh.common.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

val LocalReceiptCalendarStyle = compositionLocalOf { ReceiptCalendarStyle.AFGHAN_SOLAR }

val LocalReceiptLocale = compositionLocalOf<AppLocale?> { null }

data class ReceiptDateTimeContext(
    val locale: AppLocale,
    val calendarStyle: ReceiptCalendarStyle,
)

object ReceiptDateTimeContexts {
    @Composable
    fun current(): ReceiptDateTimeContext = ReceiptDateTimeContext(
        locale = LocalReceiptLocale.current
            ?: AppLocale.fromTag(LocalConfiguration.current.locales[0]?.toLanguageTag()),
        calendarStyle = LocalReceiptCalendarStyle.current,
    )
}
