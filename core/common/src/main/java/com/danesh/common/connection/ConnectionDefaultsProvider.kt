package com.danesh.common.connection

interface ConnectionDefaultsProvider {
    val ip: String
    val port: Int
    val nii: String

    val tmsIp: String
    val tmsPort: Int
}
