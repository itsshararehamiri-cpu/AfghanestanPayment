package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.model.PosAdminMenuAction
import com.danesh.settings.model.posAdminMenuActions
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.button.OutlinedActionButton

@Composable
fun PosAdminMenuSheetContent(
    selectedIndex: Int,
    onActionSelected: (PosAdminMenuAction) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SettingsSheetDragHandle()

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSheetHeader(
            title = stringResource(R.string.settings_pos_admin_menu_title),
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.settings_pos_admin_menu_hint),
            color = SettingsColors.DragHandle,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        posAdminMenuActions.forEachIndexed { index, action ->
            val isSelected = index == selectedIndex
            OutlinedActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    color = Color(0x2214BDF6),
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = SettingsColors.Accent.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(14.dp),
                                )
                        } else {
                            Modifier
                        },
                    ),
                text = stringResource(action.labelRes),
                onClick = { onActionSelected(action) },
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun PosAdminMenuSheetContentPreview() {
    PosAdminMenuSheetContent(
        selectedIndex = 1,
        onActionSelected = {},
        onCloseClick = {},
    )
}
