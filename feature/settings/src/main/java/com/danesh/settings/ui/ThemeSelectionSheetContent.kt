package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.settings.model.AppThemeMode

@Composable
fun ThemeSelectionSheetContent(
    selectedTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
    ) {
        SettingsSheetDragHandle()

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSheetHeader(
            title = stringResource(R.string.settings_theme_sheet_title),
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSelectionOptionRow(
            label = stringResource(R.string.settings_theme_dark),
            icon = Icons.Outlined.DarkMode,
            iconContentDescription = stringResource(R.string.settings_theme_dark_icon),
            isSelected = selectedTheme == AppThemeMode.Dark,
            onClick = { onThemeSelected(AppThemeMode.Dark) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSelectionOptionRow(
            label = stringResource(R.string.settings_theme_light),
            icon = Icons.Outlined.LightMode,
            iconContentDescription = stringResource(R.string.settings_theme_light_icon),
            isSelected = selectedTheme == AppThemeMode.Light,
            onClick = { onThemeSelected(AppThemeMode.Light) },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun ThemeSelectionSheetContentPreview() {
    ThemeSelectionSheetContent(
        selectedTheme = AppThemeMode.Dark,
        onThemeSelected = {},
        onCloseClick = {},
    )
}
