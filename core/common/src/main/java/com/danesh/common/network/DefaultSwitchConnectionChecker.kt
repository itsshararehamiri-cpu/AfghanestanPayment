package com.danesh.common.network

import com.danesh.common.connection.ConnectionAddressValidator
import com.danesh.common.connection.ConnectionPreferences
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * بررسی وضعیت اتصال به سوییچ با Telnet (اتصال TCP به IP/Port سرور).
 */
@Singleton
class DefaultSwitchConnectionChecker @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
) : SwitchConnectionChecker {

    override suspend fun isServerReachable(): Boolean = withContext(Dispatchers.IO) {
        val ip = connectionPreferences.getIp().trim()
        val port = connectionPreferences.getPort()
        if (!ConnectionAddressValidator.isMainServerConfigured(ip, port)) {
            return@withContext false
        }
        runCatching {
            Socket().use { socket ->
                socket.tcpNoDelay = true
                socket.soTimeout = CONNECT_TIMEOUT_MS
                socket.connect(InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS)
                socket.isConnected && !socket.isClosed
            }
        }.getOrDefault(false)
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 10_000
    }
}
