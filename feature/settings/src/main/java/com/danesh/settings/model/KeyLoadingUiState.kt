package com.danesh.settings.model

import com.danesh.api.InitDefaults

data class KeyLoadingUiState(
    val requiresBallotTickets: Boolean = true,
    val firstBallotTicket: String = InitDefaults.FIRST_BALLOT_TICKET,
    val secondBallotTicket: String = InitDefaults.SECOND_BALLOT_TICKET,
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val kcvSummary: KeyLoadingKcvSummary? = null,
    val isSuccess: Boolean = false,
)
