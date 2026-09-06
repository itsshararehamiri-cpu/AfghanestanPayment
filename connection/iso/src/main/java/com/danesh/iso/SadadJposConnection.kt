package com.danesh.iso

import android.util.Log
import com.danesh.common.connection.ConnectionEndpointResolver
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.core.Connection
import com.danesh.iso.packager.SadadIso93BPackager
import kotlinx.coroutines.delay
import org.jpos.iso.ISOPackager
import javax.inject.Inject

private const val TAG = "SadadJposConnection"

/**
 * اتصال ISO8583 سداد — قالب فریم NAC استاندارد شتاب (طول 2 بایتی + TPDU 5 بایتی مبتنی بر NII،
 * مطابق [BpWireFrame])، همراه با packager اختصاصی سداد ([SadadIso93BPackager]).
 */
class SadadJposConnection @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val endpointResolver: ConnectionEndpointResolver,
    private val messageProvider: IsoMessageProvider,
) : Connection<IsoMessage> {

    private lateinit var ip: String
    private var port: Int = -1
    private lateinit var channel: NACChannel3
    private val packager: ISOPackager = SadadIso93BPackager()

    override suspend fun connect() {
        val nii = connectionPreferences.getNii()
        IsoConnectionFailover.connectWithFailover(endpointResolver) { endpoint ->
            connectToEndpoint(endpoint.ip, endpoint.port, nii)
        }
    }

    override suspend fun init(ip: String, port: Int, nii: String) {
        val header = BpWireFrame.buildTpdu(nii)
        this.ip = ip
        this.port = port
        channel = NACChannel3(ip, port, packager, header)
    }

    override suspend fun request(message: IsoMessage, isEcho: Boolean): IsoMessage? {
        try {
            channel.timeout = if (isEcho) 10000 else 60000
            channel.send(message.getIsoMessage())
        } catch (e: Exception) {
            Log.e(TAG, "Error during request: ${e.message}")
            reconnect()
            throw Exception("Start channel failed", e)
        }

        val received = channel.receive()
        val response = messageProvider.create()
        response.toIsoMessage(received)
        return response
    }

    override fun stop() {
        channel.disconnect()
    }

    private suspend fun reconnect() {
        try {
            stop()
            delay(1000)
            connect()
        } catch (e: Exception) {
            Log.e(TAG, "Reconnection failed: ${e.message}")
        }
    }

    override suspend fun start() {
        try {
            val nii = connectionPreferences.getNii()
            IsoConnectionFailover.connectWithFailover(endpointResolver) { endpoint ->
                connectToEndpoint(endpoint.ip, endpoint.port, nii, timeoutMs = 60000)
                delay(1000)
            }
        } catch (e: Exception) {
            Log.e(TAG, "start failed: {$ip : $port}", e)
            throw Exception("Start channel failed", e)
        }
    }

    override suspend fun send(message: IsoMessage) {
        try {
            channel.send(message.getIsoMessage())
        } catch (e: Exception) {
            Log.e(TAG, "send failed: ${e.message}")
            throw Exception("Start channel failed", e)
        }
    }

    override suspend fun receive(): IsoMessage? {
        return try {
            val received = channel.receive()
            messageProvider.create().also { it.toIsoMessage(received) }
        } catch (e: Exception) {
            Log.e(TAG, "receive failed: ${e.message}", e)
            throw e
        }
    }

    override fun close() {
        channel.disconnect()
    }

    private fun connectToEndpoint(
        endpointIp: String,
        endpointPort: Int,
        nii: String,
        timeoutMs: Int = 30000,
    ) {
        val header = BpWireFrame.buildTpdu(nii)
        ip = endpointIp
        port = endpointPort
        channel = NACChannel3(endpointIp, endpointPort, packager, header)
        channel.name = "sipa"
        channel.packager = packager
        channel.timeout = timeoutMs
        channel.connect()
    }
}
