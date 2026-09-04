package com.danesh.api

enum class BallotType {
    FIRST,
    SECOND,
}

data class InitInput(
    val ballotType: BallotType = BallotType.FIRST,
    val firstBallotTicket: String = InitDefaults.FIRST_BALLOT_TICKET,
    val secondBallotTicket: String = InitDefaults.SECOND_BALLOT_TICKET,
)
