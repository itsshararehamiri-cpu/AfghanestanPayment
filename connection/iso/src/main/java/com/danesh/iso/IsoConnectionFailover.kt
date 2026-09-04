package com.danesh.iso

import android.util.Log
import com.danesh.common.connection.ConnectionEndpoint
import com.danesh.common.connection.ConnectionEndpointResolver

object IsoConnectionFailover {

    private const val TAG = "IsoConnectionFailover"

    suspend fun connectWithFailover(
        endpointResolver: ConnectionEndpointResolver,
        connectTo: suspend (ConnectionEndpoint) -> Unit,
    ) {
        val endpoints = endpointResolver.resolveForTransaction()
        if (endpoints.isEmpty()) {
            throw Exception("No connection endpoint configured")
        }

        var lastError: Exception? = null
        for ((index, endpoint) in endpoints.withIndex()) {
            try {
                connectTo(endpoint)
                if (index > 0) {
                    Log.i(TAG, "Connected via backup ${endpoint.channel}: ${endpoint.ip}:${endpoint.port}")
                }
                return
            } catch (e: Exception) {
                lastError = e
                Log.w(
                    TAG,
                    "Connection failed on ${endpoint.channel} ${endpoint.ip}:${endpoint.port}: ${e.message}",
                )
            }
        }
        throw Exception("Start channel failed", lastError)
    }
}
