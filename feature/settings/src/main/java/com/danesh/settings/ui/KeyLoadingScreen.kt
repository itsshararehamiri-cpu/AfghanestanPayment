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
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.model.KeyLoadingUiState
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun KeyLoadingScreen(
    title: String,
    uiState: KeyLoadingUiState,
    onBackClick: () -> Unit,
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
            .appScreenBackground(),
    ) {
        Toolbar(
            title = title,
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsSectionTitle(
                title = stringResource(R.string.settings_key_loading_section_title),
            )

//            Text(
//                text = stringResource(R.string.settings_key_loading_description),
//                color = SettingsColors.TextPrimary,
//                fontSize = 14.sp,
//                style = MaterialTheme.typography.bodySmall,
//            )

            if (uiState.requiresBallotTickets) {
                Spacer(modifier = Modifier.height(16.dp))

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
                    color = if (uiState.isSuccess) SettingsColors.Accent else SettingsColors.TextPrimary,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            uiState.kcvSummary?.takeIf { it.master != "-" }?.let { kcvSummary ->
                Spacer(modifier = Modifier.height(16.dp))
                KeyLoadingKcvTable(kcvSummary = kcvSummary)
            }

            Spacer(modifier = Modifier.height(36.dp))

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
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun KeyLoadingScreenPreview() {
    KeyLoadingScreen(
        title = "دریافت کلید",
        uiState = KeyLoadingUiState(
            kcvSummary = KeyLoadingKcvSummary(master = "A1B2C3"),
            isSuccess = true,
            resultMessage = "دریافت کلید با موفقیت انجام شد",
        ),
        onBackClick = {},
        onFirstBallotTicketChange = {},
        onSecondBallotTicketChange = {},
        onScanFirstBallotTicket = {},
        onScanSecondBallotTicket = {},
        onConfirmClick = {},
        onCancelClick = {},
    )
}
