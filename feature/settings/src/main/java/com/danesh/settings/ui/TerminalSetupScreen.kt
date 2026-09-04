package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.InitialConfigurationUiState
import com.danesh.settings.model.TerminalSetupPhase
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun TerminalSetupScreen(
    uiState: InitialConfigurationUiState,
    onBackClick: () -> Unit,
    onFirstBallotTicketChange: (String) -> Unit,
    onSecondBallotTicketChange: (String) -> Unit,
    onScanFirstBallotTicket: () -> Unit,
    onScanSecondBallotTicket: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSummaryConfirm: () -> Unit,
    onPrintClick: () -> Unit,
    onExecuteClick: () -> Unit,
    showSummaryOverlay: Boolean = false,
    onDismissSummaryOverlay: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_terminal_setup),
            onBackClick = {
                when {
                    uiState.summary != null -> onSummaryConfirm()
                    uiState.phase == TerminalSetupPhase.EXECUTE_FORM -> onBackClick()
                    else -> onBackClick()
                }
            },
        )

        when {
            uiState.summary != null && uiState.phase != TerminalSetupPhase.ACTION_CHOOSER -> {
                InitialConfigurationSummaryContent(
                    summary = uiState.summary,
                    onConfirmClick = onSummaryConfirm,
                )
            }
            uiState.phase == TerminalSetupPhase.ACTION_CHOOSER -> {
                TerminalSetupActionChooserContent(
                    uiState = uiState,
                    onPrintClick = onPrintClick,
                    onExecuteClick = onExecuteClick,
                )
            }
            else -> {
                TerminalSetupExecuteFormContent(
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

    if (showSummaryOverlay && uiState.summary != null) {
        ConfigurationSummaryOverlay(
            summary = uiState.summary,
            onDismissRequest = onDismissSummaryOverlay,
        )
    }
}

@Composable
private fun TerminalSetupActionChooserContent(
    uiState: InitialConfigurationUiState,
    onPrintClick: () -> Unit,
    onExecuteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
        SettingsSectionTitle(
            title = stringResource(R.string.settings_terminal_setup),
        )

//        Text(
//            text = stringResource(R.string.settings_terminal_setup_logon_description),
//            color = SettingsColors.TextPrimary,
//            fontSize = 14.sp,
//            style = MaterialTheme.typography.bodyMedium,
//        )

        uiState.resultMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = SettingsColors.Accent,
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
            text = stringResource(R.string.settings_terminal_setup_execute),
            onClick = {
                if (!uiState.isLoading) {
                    onExecuteClick()
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = stringResource(R.string.settings_terminal_setup_print),
            onClick = {
                if (!uiState.isLoading) {
                    onPrintClick()
                }
            },
        )
    }
}

@Composable
private fun TerminalSetupExecuteFormContent(
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
//        SettingsSectionTitle(
//            title = stringResource(R.string.settings_terminal_setup),
//        )

//        if (uiState.usesLogonSetup) {
//            Text(
//                text = stringResource(R.string.settings_terminal_setup_logon_description),
//                color = SettingsColors.TextPrimary,
//                fontSize = 14.sp,
//                style = MaterialTheme.typography.bodyMedium,
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//        }

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
        Spacer(modifier = Modifier.height(24.dp))

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
            title = stringResource(R.string.settings_terminal_setup_success),
        )

        ConfigurationSummaryTable(summary = summary)

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

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun TerminalSetupActionChooserPreview() {
    TerminalSetupScreen(
        uiState = InitialConfigurationUiState(phase = TerminalSetupPhase.ACTION_CHOOSER),
        onBackClick = {},
        onFirstBallotTicketChange = {},
        onSecondBallotTicketChange = {},
        onScanFirstBallotTicket = {},
        onScanSecondBallotTicket = {},
        onConfirmClick = {},
        onCancelClick = {},
        onSummaryConfirm = {},
        onPrintClick = {},
        onExecuteClick = {},
    )
}
