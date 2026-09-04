package com.danesh.common.connection

data class ConnectionEndpoint(
    val channel: ConnectionChannel,
    val ip: String,
    val port: Int,
)
