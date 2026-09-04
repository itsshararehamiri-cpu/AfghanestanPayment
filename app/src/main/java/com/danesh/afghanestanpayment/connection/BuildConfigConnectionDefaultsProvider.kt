package com.danesh.afghanestanpayment.connection

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.common.connection.ConnectionDefaults
import com.danesh.common.connection.ConnectionDefaultsProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigConnectionDefaultsProvider @Inject constructor(

) : ConnectionDefaultsProvider {
    override val ip: String = BuildConfig.DEFAULT_SERVER_IP
    override val port: Int = BuildConfig.DEFAULT_SERVER_PORT
    override val nii: String = ConnectionDefaults.NII


    override val tmsIp: String=BuildConfig.DEFAULT_SERVER_IP
    override val tmsPort: Int= BuildConfig.DEFAULT_SERVER_PORT
}
