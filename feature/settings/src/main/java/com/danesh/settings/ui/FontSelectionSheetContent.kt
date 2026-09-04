package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.settings.model.AppFontFamily

@Composable
fun FontSelectionSheetContent(
    selectedFont: AppFontFamily,
    onFontSelected: (AppFontFamily) -> Unit,
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
            title = stringResource(R.string.settings_font_sheet_title),
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSelectionOptionRow(
            label = stringResource(R.string.settings_font_yekan_bakh),
            isSelected = selectedFont == AppFontFamily.YekanBakh,
            onClick = { onFontSelected(AppFontFamily.YekanBakh) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSelectionOptionRow(
            label = stringResource(R.string.settings_font_sans_serif),
            isSelected = selectedFont == AppFontFamily.SansSerif,
            onClick = { onFontSelected(AppFontFamily.SansSerif) },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun FontSelectionSheetContentPreview() {
    FontSelectionSheetContent(
        selectedFont = AppFontFamily.YekanBakh,
        onFontSelected = {},
        onCloseClick = {},
    )
}
