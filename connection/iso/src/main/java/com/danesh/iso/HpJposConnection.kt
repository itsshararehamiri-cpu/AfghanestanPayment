package com.danesh.iso

import android.util.Log
import com.danesh.common.connection.ConnectionEndpointResolver
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.core.Connection
import com.danesh.iso.packager.HpIso93BPackager
import com.danesh.iso.packager.HpTerminalConfigResponsePackager
import kotlinx.coroutines.delay
import org.jpos.iso.ISOPackager
import org.jpos.util.Logger
import org.jpos.util.SimpleLogListener
import javax.inject.Inject

private const val TAG = "HpJposConnection"
private const val TERMINAL_CONFIG_MTI = "1304"
private const val TERMINAL_CONFIG_FUNCTION_CODE = "305"

class HpJposConnection @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val endpointResolver: ConnectionEndpointResolver,
    private val messageProvider: IsoMessageProvider,
) : Connection<IsoMessage> {

    private lateinit var ip: String
    private var port: Int = -1
    private lateinit var channel: NACChannel2
    private val packager: ISOPackager = HpIso93BPackager()
    private val terminalConfigResponsePackager: ISOPackager = HpTerminalConfigResponsePackager()

    /** packager پاسخِ آخرین پیام ارسال‌شده؛ موتور تراکنش [send] و [receive] را جدا صدا می‌زند. */
    @Volatile
    private var pendingResponsePackager: ISOPackager = packager

    override suspend fun connect() {
        val nii = connectionPreferences.getNii()
        IsoConnectionFailover.connectWithFailover(endpointResolver) { endpoint ->
            connectToEndpoint(endpoint.ip, endpoint.port, nii)
        }
        Log.d(TAG, "init() called with: ip = $ip, port = $port, nii = ")

    }

    override suspend fun init(ip: String, port: Int, nii: String) {
        val header = buildHpTpdu(nii)
        this.ip = ip
        this.port = port

        channel = NACChannel2(ip, port, packager, header)
    }

    override suspend fun request(message: IsoMessage, isEcho: Boolean): IsoMessage? {
        try {
            channel.timeout = if (isEcho) 10000 else 60000
            pendingResponsePackager = responsePackagerFor(message)
            channel.send(message.getIsoMessage())
        } catch (e: Exception) {
            Log.e(TAG, "Error during request: ${e.message}")
            reconnect()
            throw Exception("Start channel failed", e)
        }

        val received = channel.receive(takePendingResponsePackager())
        val response = messageProvider.create()
        response.toIsoMessage(received)
        return response
    }

    /**
     * پاسخ 1314 پیکربندی ترمینال (درخواست 1304 با DE24=305) DE43 را با طول LLL می‌فرستد؛
     * فقط برای همین تراکنش packager اختصاصی به‌کار می‌رود و بقیه با packager پیش‌فرض unpack می‌شوند.
     */
    private fun responsePackagerFor(message: IsoMessage): ISOPackager {
        val isTerminalConfig = message.mti == TERMINAL_CONFIG_MTI &&
            message.nii.trim() == TERMINAL_CONFIG_FUNCTION_CODE
        return if (isTerminalConfig) terminalConfigResponsePackager else packager
    }

    private fun takePendingResponsePackager(): ISOPackager {
        val responsePackager = pendingResponsePackager
        pendingResponsePackager = packager
        return responsePackager
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
            pendingResponsePackager = responsePackagerFor(message)
            channel.send(message.getIsoMessage())
        } catch (e: Exception) {
            Log.e(TAG, "send failed: ${e.message}")
            throw Exception("Start channel failed", e)
        }
    }

    override suspend fun receive(): IsoMessage? {
        return try {
            val received = channel.receive(takePendingResponsePackager())
            messageProvider.create().also { it.toIsoMessage(received) }
        } catch (e: Exception) {
            Log.e(TAG, "receive failed: ${e.message}", e)
            throw e
        }
    }

    override fun close() {
        channel.disconnect()
    }

    private fun attachChannelLogger() {
        val logger = Logger()
        logger.addListener(SimpleLogListener())
        logger.name = "testLinkLogger"
        channel.setLogger(logger, "POS")
    }

    private fun connectToEndpoint(
        endpointIp: String,
        endpointPort: Int,
        nii: String,
        timeoutMs: Int = 30000,
    ) {
        val header = buildHpTpdu(nii)
        ip = endpointIp
        port = endpointPort
        channel = NACChannel2(endpointIp, endpointPort, packager, header)
        channel.name = "sipa"
        channel.packager = packager
        attachChannelLogger()
        channel.timeout = timeoutMs
        channel.connect()
    }

    private fun buildHpTpdu(nii: String): ByteArray {
        val niiHex = ByteUtil.padleft(nii.trim().toInt().toString(16), 4, '0')
        Log.d(TAG, "buildHpTpdu: $nii")
        Log.d(TAG, "buildHpTpdu: $niiHex")

        //return ByteUtil.hex2byte("60$niiHex" + "0000")
        return ByteUtil.hex2byte("6018191270")
        /*
        val niiHex = ByteUtil.padleft(nii.trim().toInt().toString(16), 4, '0')
        return ByteUtil.hex2byte("60$niiHex" + "0000")

         */
    }
}
