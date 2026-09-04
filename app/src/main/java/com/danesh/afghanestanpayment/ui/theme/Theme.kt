package com.danesh.afghanestanpayment.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.danesh.afghanestanpayment.R
import com.danesh.ui.theme.AppFontFamilies
import com.danesh.ui.theme.AppTheme
import com.danesh.ui.theme.LocalAppFontFamilies

private val RtlFontFamily = FontFamily(
    Font(R.font.iranyekanblackfanum),
)

private val LtrFontFamily = FontFamily(
    Font(R.font.inter),
)

@Composable
fun AfghanestanPaymentTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppFontFamilies provides AppFontFamilies(
            rtl = RtlFontFamily,
            ltr = LtrFontFamily,
        ),
    ) {
        AppTheme {
            content()
        }
    }
}
