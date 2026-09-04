package com.danesh.common.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberCurrentAppLanguage(): AppLanguage {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag()
    return remember(localeTag) { AppLanguage.fromTag(localeTag) }
}

@Composable
fun displayMerchantName(
    persianName: String,
    englishName: String,
): String {
    val language = rememberCurrentAppLanguage()
    return remember(persianName, englishName, language) {
        MerchantNameDisplay.resolve(persianName, englishName, language)
    }
}
