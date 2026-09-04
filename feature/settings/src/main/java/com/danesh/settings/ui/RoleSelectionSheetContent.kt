package com.danesh.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.settings.model.AppRole
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.button.OutlinedActionButton

@Composable
fun RoleSelectionSheetContent(
    onRoleSelected: (AppRole) -> Unit,
    onCloseClick: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SettingsSheetDragHandle()

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSheetHeader(
            title = stringResource(R.string.settings_role_sheet_title),
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(R.drawable.ic_role_illustration),
            contentDescription = stringResource(R.string.settings_role_illustration),
            modifier = Modifier.height(140.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = stringResource(R.string.settings_role_support),
            onClick = { onRoleSelected(AppRole.Support) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = stringResource(R.string.settings_role_merchant),
            onClick = { onRoleSelected(AppRole.Merchant) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(
                R.string.settings_role_sheet_app_version,
                appVersion.ifBlank { "-" },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = SettingsColors.DragHandle,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun RoleSelectionSheetContentPreview() {
    RoleSelectionSheetContent(
        onRoleSelected = {},
        onCloseClick = {},
        appVersion = "1.0.0",
    )
}
