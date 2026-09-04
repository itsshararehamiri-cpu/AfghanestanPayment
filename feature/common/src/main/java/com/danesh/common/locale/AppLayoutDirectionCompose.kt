package com.danesh.common.locale

import androidx.compose.ui.unit.LayoutDirection

fun AppLayoutDirection.toComposeLayoutDirection(): LayoutDirection = when (this) {
    AppLayoutDirection.Rtl -> LayoutDirection.Rtl
    AppLayoutDirection.Ltr -> LayoutDirection.Ltr
}

fun AppLanguage.toComposeLayoutDirection(): LayoutDirection =
    layoutDirection.toComposeLayoutDirection()
