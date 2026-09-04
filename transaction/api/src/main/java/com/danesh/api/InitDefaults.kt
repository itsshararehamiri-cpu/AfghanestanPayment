package com.danesh.api


object InitDefaults {
    const val FIRST_BALLOT_TICKET = "123456"
    const val SECOND_BALLOT_TICKET = "123456"

    fun ticketFor(ballotType: BallotType): String = when (ballotType) {
        BallotType.FIRST -> FIRST_BALLOT_TICKET
        BallotType.SECOND -> SECOND_BALLOT_TICKET
    }
}
