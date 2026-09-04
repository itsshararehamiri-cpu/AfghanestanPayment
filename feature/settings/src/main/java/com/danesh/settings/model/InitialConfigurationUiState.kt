package com.danesh.settings.model

import com.danesh.api.InitDefaults

data class InitialConfigurationSummary(
    val hardwareSerial: String,
    val terminalId: String,
    val appVersion: String,
    val programDate: String,
    val merchantId: String,
)

enum class TerminalSetupPhase {
    ACTION_CHOOSER,
    EXECUTE_FORM,
}

data class InitialConfigurationUiState(
    val firstBallotTicket: String = InitDefaults.FIRST_BALLOT_TICKET,
    val secondBallotTicket: String = InitDefaults.SECOND_BALLOT_TICKET,
    val requiresBallotTickets: Boolean = false,
    val usesLogonSetup: Boolean = false,
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val summary: InitialConfigurationSummary? = null,
    val phase: TerminalSetupPhase = TerminalSetupPhase.EXECUTE_FORM,
)
