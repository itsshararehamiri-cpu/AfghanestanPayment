package com.danesh.hp.config

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.PspDeviceOperations
import com.danesh.hp.device.HpDeviceWorkflow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpInitialConfigurationPolicy @Inject constructor(
    private val deviceOperations: PspDeviceOperations,
    private val deviceWorkflow: HpDeviceWorkflow,
    private val configurationStore: DeviceConfigurationStore,
) : InitialConfigurationPolicy {

    override val requiresBallotTickets: Boolean = false
    override val usesTerminalConfigFlow: Boolean = true

    override suspend fun injectKeys(): Result<Unit> = runCatching {
        deviceOperations.completeLogon(deviceWorkflow.hardcodedWorkingKeys())
        configurationStore.markConfigured()
    }
}
