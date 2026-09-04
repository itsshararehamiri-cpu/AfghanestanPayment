package com.danesh.bp.config

import com.danesh.api.InitialConfigurationPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpInitialConfigurationPolicy @Inject constructor() : InitialConfigurationPolicy {
    override val requiresBallotTickets: Boolean = true
    override val usesTerminalSetupLogon: Boolean = true
}
