package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.InitialConfigurationUiState
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun InitialConfigurationScreen(
    uiState: InitialConfigurationUiState,
    onBackClick: () -> Unit,
    onFirstBallotTicketChange: (String) -> Unit,
    onSecondBallotTicketChange: (String) -> Unit,
    onScanFirstBallotTicket: () -> Unit,
    onScanSecondBallotTicket: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSummaryConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_initial_configuration),
            onBackClick = {
                if (uiState.summary != null) {
                    onSummaryConfirm()
                } else {
                    onBackClick()
                }
            },
        )

        if (uiState.summary != null) {
            InitialConfigurationSummaryContent(
                summary = uiState.summary,
                onConfirmClick = onSummaryConfirm,
            )
        } else {
            InitialConfigurationFormContent(
                uiState = uiState,
                onFirstBallotTicketChange = onFirstBallotTicketChange,
                onSecondBallotTicketChange = onSecondBallotTicketChange,
                onScanFirstBallotTicket = onScanFirstBallotTicket,
                onScanSecondBallotTicket = onScanSecondBallotTicket,
                onConfirmClick = onConfirmClick,
                onCancelClick = onCancelClick,
            )
        }
    }
}

@Composable
private fun InitialConfigurationSummaryContent(
    summary: InitialConfigurationSummary,
    onConfirmClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
        SettingsSectionTitle(
            title = stringResource(R.string.settings_initial_configuration_summary_title),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF0C2C36).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF173A46),
                            Color(0xFF14BDF6).copy(alpha = 0.5f),
                            Color(0xFF35B7E4).copy(alpha = 0.5f),
                        ),
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ConfigurationSummaryRow(
                label = stringResource(R.string.settings_initial_configuration_hardware_serial),
                value = summary.hardwareSerial,
            )
            ConfigurationSummaryRow(
                label = stringResource(R.string.settings_initial_configuration_terminal_id),
                value = summary.terminalId,
            )
            ConfigurationSummaryRow(
                label = stringResource(R.string.settings_initial_configuration_app_version),
                value = summary.appVersion,
            )
            ConfigurationSummaryRow(
                label = stringResource(R.string.settings_initial_configuration_program_date),
                value = summary.programDate,
            )
            ConfigurationSummaryRow(
                label = stringResource(R.string.settings_initial_configuration_merchant_id),
                value = summary.merchantId,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = stringResource(R.string.settings_confirm),
            onClick = onConfirmClick,
        )
    }
}

@Composable
private fun ConfigurationSummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = SettingsColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            color = SettingsColors.Accent,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun InitialConfigurationFormContent(
    uiState: InitialConfigurationUiState,
    onFirstBallotTicketChange: (String) -> Unit,
    onSecondBallotTicketChange: (String) -> Unit,
    onScanFirstBallotTicket: () -> Unit,
    onScanSecondBallotTicket: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
//        SettingsSectionTitle(
//            title = if (uiState.requiresBallotTickets) {
//                stringResource(R.string.settings_ballot_selection_title)
//            } else {
//                stringResource(R.string.settings_initial_configuration)
//            },
//        )

        if (uiState.requiresBallotTickets) {
            Text(
                text = stringResource(R.string.settings_ballot_first),
            color = SettingsColors.TextPrimary,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTextField(
            value = uiState.firstBallotTicket,
            onValueChange = onFirstBallotTicketChange,
            placeholder = stringResource(R.string.settings_ticket_placeholder),
            enabled = !uiState.isLoading,
            inputFilter = SettingsTextInputFilter.DigitsOnly,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedActionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.settings_ticket_scan_qr),
            onClick = {
                if (!uiState.isLoading) {
                    onScanFirstBallotTicket()
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_ballot_second),
            color = SettingsColors.TextPrimary,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTextField(
            value = uiState.secondBallotTicket,
            onValueChange = onSecondBallotTicketChange,
            placeholder = stringResource(R.string.settings_ticket_placeholder),
            enabled = !uiState.isLoading,
            inputFilter = SettingsTextInputFilter.DigitsOnly,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedActionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.settings_ticket_scan_qr),
            onClick = {
                if (!uiState.isLoading) {
                    onScanSecondBallotTicket()
                }
            },
        )
        } else {
            Text(
                text = stringResource(R.string.settings_initial_configuration_hp_description),
                color = SettingsColors.TextPrimary,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        uiState.resultMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = SettingsColors.TextPrimary,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
                color = SettingsColors.Accent,
            )
        }

        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = stringResource(R.string.settings_confirm),
            onClick = {
                if (!uiState.isLoading) {
                    onConfirmClick()
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            text = stringResource(R.string.settings_cancel),
            onClick = onCancelClick,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun InitialConfigurationScreenPreview() {
    InitialConfigurationScreen(
        uiState = InitialConfigurationUiState(),
        onBackClick = {},
        onFirstBallotTicketChange = {},
        onSecondBallotTicketChange = {},
        onScanFirstBallotTicket = {},
        onScanSecondBallotTicket = {},
        onConfirmClick = {},
        onCancelClick = {},
        onSummaryConfirm = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun InitialConfigurationSummaryPreview() {
    InitialConfigurationScreen(
        uiState = InitialConfigurationUiState(
            summary = InitialConfigurationSummary(
                hardwareSerial = "123456789",
                terminalId = "12533864",
                appVersion = "1.0.0",
                programDate = "2026/07/29 11:09",
                merchantId = "44236789",//44236789
            ),
        ),
        onBackClick = {},
        onFirstBallotTicketChange = {},
        onSecondBallotTicketChange = {},
        onScanFirstBallotTicket = {},
        onScanSecondBallotTicket = {},
        onConfirmClick = {},
        onCancelClick = {},
        onSummaryConfirm = {},
    )
}
