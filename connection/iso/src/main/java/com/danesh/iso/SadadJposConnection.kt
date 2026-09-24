package com.danesh.iso

import android.util.Log
import com.danesh.common.connection.ConnectionEndpointResolver
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.core.Connection
import com.danesh.iso.packager.SadadIso93BPackager
import kotlinx.coroutines.delay
import org.jpos.iso.ISOPackager
import org.jpos.iso.ISOUtil
import org.jpos.iso.channel.NACChannel
import javax.inject.Inject

private const val TAG = "SadadJposConnection"

class SadadJposConnection @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val endpointResolver: ConnectionEndpointResolver,
    private val messageProvider: IsoMessageProvider,
) : Connection<IsoMessage> {

    private lateinit var ip: String
    private var port: Int = -1
    private lateinit var channel: NACChannel4
    private val packager: ISOPackager = SadadIso93BPackager()

    override suspend fun connect() {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection1${connectionPreferences.getIp()}")
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection1${connectionPreferences.getPort().toString()}")
        Log.d(TAG, "SadadJposConnection.connect")

        val header = SadadWireFrame.buildTpdu("")
        this.ip = connectionPreferences.getIp()
        this.port = connectionPreferences.getPort()
        Log.d(TAG, "SADAD connect ip=$ip port=$port nii=")
        Log.d(TAG, "SADAD TPDU=${ISOUtil.hexString(header)}")

        channel = NACChannel4(ip, port, packager, header)
        val nii = connectionPreferences.getNii()
//        IsoConnectionFailover.connectWithFailover(endpointResolver) { endpoint ->
//            connectToEndpoint(endpoint.ip, endpoint.port, nii)
//        }
        channel.connect()
        Log.d(TAG, "SADAD channel connected=${channel.isConnected}")

    }

    override suspend fun init(ip: String, port: Int, nii: String) {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection2${ip}")
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection2${port}")

        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection2")

        val header = SadadWireFrame.buildTpdu(nii)
        this.ip = ip
        this.port = port
        channel = NACChannel4(ip, port, packager, header)
    }

    override suspend fun request(message: IsoMessage, isEcho: Boolean): IsoMessage? {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection3")
        try {
            channel.timeout = 10000
            Log.d(TAG, "CONNECTED BEFORE SEND = ${channel.isConnected}")
            channel.send(message.getIsoMessage())
            Log.d(TAG, "CONNECTED AFTER SEND = ${channel.isConnected}")
        } catch (e: Exception) {
            Log.e(TAG, "Error during request: ${e.message}")
            reconnect()
            throw Exception("Start channel failed", e)
        }
        Log.d(TAG, "CONNECTED BEFORE RECEIVE = ${channel.isConnected}")
        val received = channel.receive()
        Log.d(TAG, "CONNECTED BEFORE RECEIVEa = ${channel.isConnected}")
        val response = messageProvider.create()
        response.toIsoMessage(received)
        response.print("resp>>")
      //  channel.lastReceivedIsoBody?.let { raw -> response.requireSadad().setRawPackedBody(raw) }
        return response
    }

    override fun stop() {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection4")
        channel.disconnect()
    }

    private suspend fun reconnect() {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection5")
        try {
            stop()
            delay(1000)
            connect()
        } catch (e: Exception) {
            Log.e(TAG, "Reconnection failed: ${e.message}")
        }
    }

    override suspend fun start() {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection6")
        try {
            val nii = connectionPreferences.getNii()
//            IsoConnectionFailover.connectWithFailover(endpointResolver) { endpoint ->
//                connectToEndpoint(endpoint.ip, endpoint.port, nii, timeoutMs = 60000)
//                delay(1000)
//            }
            channel.connect()
        } catch (e: Exception) {
            Log.e(TAG, "start failed: {$ip : $port}", e)
            throw Exception("Start channel failed", e)
        }
    }

    override suspend fun send(message: IsoMessage) {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection7")
        try {
            Log.d(TAG, "CONNECTED BEFORE SEND = ${channel.isConnected}")
            channel.send(message.getIsoMessage())
            Log.d(TAG, "CONNECTED AFTER SEND = ${channel.isConnected}")
        } catch (e: Exception) {
            Log.e(TAG, "send failed: ${e.message}")
            throw Exception("Start channel failed", e)
        }
    }

    override suspend fun receive(): IsoMessage? {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection8")
        return try {
            Log.d(TAG, "CONNECTED BEFORE RECEIVE = ${channel.isConnected}")
            val received = channel.receive()
            Log.d(TAG, "CONNECTED BEFORE RECEIVEb = ${channel.isConnected}")
         val temp=   messageProvider.create().also {
                it.toIsoMessage(received)
               // channel.lastReceivedIsoBody?.let { raw -> it.requireSadad().setRawPackedBody(raw) }
            }
            temp.print("resp>>")
            temp
        } catch (e: Exception) {
            Log.e(TAG, "receive failed: ${e.message}", e)
            throw e
        }
    }

    override fun close() {
        Log.d(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaSadadJposConnection9")
        channel.disconnect()
    }

    private fun connectToEndpoint(
        endpointIp: String,
        endpointPort: Int,
        nii: String,
        timeoutMs: Int = 30000,
    ) {
        val header = SadadWireFrame.buildTpdu(nii)
        ip = endpointIp
        port = endpointPort
        channel = NACChannel4(endpointIp, endpointPort, packager, header)
        channel.name = "sipa"
        channel.packager = packager
        channel.timeout = timeoutMs
        channel.connect()
    }
}
