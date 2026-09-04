package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Print
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.common.receipt.MerchantReceiptPrintMode
import com.danesh.settings.R
import com.danesh.settings.model.labelRes

@Composable
fun MerchantReceiptPrintSelectionSheetContent(
    selectedMode: MerchantReceiptPrintMode,
    onModeSelected: (MerchantReceiptPrintMode) -> Unit,
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
            title = stringResource(R.string.settings_merchant_receipt_sheet_title),
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSelectionOptionRow(
            label = stringResource(MerchantReceiptPrintMode.OPTIONAL.labelRes()),
            icon = Icons.Outlined.Print,
            iconContentDescription = stringResource(MerchantReceiptPrintMode.OPTIONAL.labelRes()),
            isSelected = selectedMode == MerchantReceiptPrintMode.OPTIONAL,
            onClick = { onModeSelected(MerchantReceiptPrintMode.OPTIONAL) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSelectionOptionRow(
            label = stringResource(MerchantReceiptPrintMode.MANDATORY.labelRes()),
            icon = Icons.Outlined.CheckCircle,
            iconContentDescription = stringResource(MerchantReceiptPrintMode.MANDATORY.labelRes()),
            isSelected = selectedMode == MerchantReceiptPrintMode.MANDATORY,
            onClick = { onModeSelected(MerchantReceiptPrintMode.MANDATORY) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSelectionOptionRow(
            label = stringResource(MerchantReceiptPrintMode.DISABLED.labelRes()),
            icon = Icons.Outlined.Block,
            iconContentDescription = stringResource(MerchantReceiptPrintMode.DISABLED.labelRes()),
            isSelected = selectedMode == MerchantReceiptPrintMode.DISABLED,
            onClick = { onModeSelected(MerchantReceiptPrintMode.DISABLED) },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun MerchantReceiptPrintSelectionSheetContentPreview() {
    MerchantReceiptPrintSelectionSheetContent(
        selectedMode = MerchantReceiptPrintMode.OPTIONAL,
        onModeSelected = {},
        onCloseClick = {},
    )
}
