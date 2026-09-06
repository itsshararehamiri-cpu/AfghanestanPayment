package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.api.KeyCardType
import com.danesh.settings.R
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.model.SadadKeyCardLoadingUiState
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun SadadKeyCardLoadingScreen(
    uiState: SadadKeyCardLoadingUiState,
    onBackClick: () -> Unit,
    onKeyIndexChange: (String) -> Unit,
    onCardAPinChange: (String) -> Unit,
    onCardBcPinChange: (String) -> Unit,
    onSelectCard: (KeyCardType) -> Unit,
    onReadCardA: () -> Unit,
    onReadCardBc: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_key_card_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsTextField(
                value = uiState.keyIndex,
                onValueChange = onKeyIndexChange,
                label = stringResource(R.string.settings_key_card_key_index_label),
                placeholder = stringResource(R.string.settings_key_card_key_index_placeholder),
                enabled = !uiState.isLoading,
                inputFilter = SettingsTextInputFilter.PositiveInteger,
            )

            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionTitle(title = stringResource(R.string.settings_key_card_step1_title))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_key_card_step1_description),
                color = SettingsColors.TextPrimary.copy(alpha = 0.8f),
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.hasStoredCardAPair) {
                    stringResource(R.string.settings_key_card_step1_stored)
                } else {
                    stringResource(R.string.settings_key_card_step1_not_stored)
                },
                color = if (uiState.hasStoredCardAPair) SettingsColors.Accent else SettingsColors.TextPrimary,
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsTextField(
                value = uiState.cardAPin,
                onValueChange = onCardAPinChange,
                label = stringResource(R.string.settings_key_card_card_a_pin_label),
                placeholder = stringResource(R.string.settings_key_card_pin_placeholder),
                enabled = !uiState.isCardAStepLoading,
                inputFilter = SettingsTextInputFilter.DigitsOnly,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_key_card_read_card_a),
                onClick = { if (!uiState.isLoading) onReadCardA() },
            )

            Spacer(modifier = Modifier.height(28.dp))
            SettingsSectionTitle(title = stringResource(R.string.settings_key_card_step2_title))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_key_card_step2_description),
                color = SettingsColors.TextPrimary.copy(alpha = 0.8f),
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CardTypeOption(
                    label = stringResource(R.string.settings_key_card_select_card_b),
                    selected = uiState.selectedCard == KeyCardType.CARD_B,
                    enabled = !uiState.isCardBcStepLoading,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectCard(KeyCardType.CARD_B) },
                )
                Spacer(modifier = Modifier.height(1.dp))
                CardTypeOption(
                    label = stringResource(R.string.settings_key_card_select_card_c),
                    selected = uiState.selectedCard == KeyCardType.CARD_C,
                    enabled = !uiState.isCardBcStepLoading,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectCard(KeyCardType.CARD_C) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsTextField(
                value = uiState.cardBcPin,
                onValueChange = onCardBcPinChange,
                label = stringResource(R.string.settings_key_card_card_bc_pin_label),
                placeholder = stringResource(R.string.settings_key_card_pin_placeholder),
                enabled = !uiState.isCardBcStepLoading,
                inputFilter = SettingsTextInputFilter.DigitsOnly,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.settings_key_card_read_card_bc),
                onClick = { if (!uiState.isLoading) onReadCardBc() },
            )

            uiState.resultMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = if (uiState.isSuccess) SettingsColors.Accent else SettingsColors.TextPrimary,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            uiState.kcvSummary?.let { summary ->
                Spacer(modifier = Modifier.height(16.dp))
                KeyLoadingKcvTable(
                    kcvSummary = KeyLoadingKcvSummary(
                        master = summary.terminalMasterKey,
                        mac = summary.mac,
                        pin = summary.pin,
                        data = summary.data,
                    ),
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    color = SettingsColors.Accent,
                )
            }
        }
    }
}

@Composable
private fun CardTypeOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(
                color = if (selected) SettingsColors.Accent.copy(alpha = 0.18f) else SettingsColors.OptionBackground,
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = if (selected) SettingsColors.Accent else SettingsColors.OptionBorder,
                shape = shape,
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = if (selected) SettingsColors.Accent else SettingsColors.TextPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
