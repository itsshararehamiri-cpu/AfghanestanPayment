package com.danesh.sadad.config

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.PspDeviceOperations
import com.danesh.sadad.device.SadadDeviceWorkflow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadInitialConfigurationPolicy @Inject constructor(
    private val deviceOperations: PspDeviceOperations,
    private val deviceWorkflow: SadadDeviceWorkflow,
    private val configurationStore: DeviceConfigurationStore,
) : InitialConfigurationPolicy {

    override val requiresBallotTickets: Boolean = false

    override suspend fun injectKeys(): Result<Unit> = runCatching {
        deviceOperations.completeLogon(deviceWorkflow.hardcodedWorkingKeys())
        configurationStore.markConfigured()
    }
}
