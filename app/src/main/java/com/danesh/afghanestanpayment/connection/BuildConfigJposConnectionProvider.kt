package com.danesh.afghanestanpayment.connection

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.core.Connection
import com.danesh.iso.BpJposConnection
import com.danesh.iso.HpJposConnection
import com.danesh.iso.IsoMessage
import com.danesh.iso.JposConnectionProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigJposConnectionProvider @Inject constructor(
    private val bpConnection: BpJposConnection,
    private val hpConnection: HpJposConnection,
) : JposConnectionProvider {

    private val delegate: Connection<IsoMessage> = when (BuildConfig.ACTIVE_PSP) {
        "BP" -> bpConnection
        else -> hpConnection
    }

    override suspend fun start() = delegate.start()

    override suspend fun send(m: IsoMessage) = delegate.send(m)

    override suspend fun connect() = delegate.connect()

    override suspend fun init(ip: String, port: Int, nii: String) = delegate.init(ip, port, nii)

    override suspend fun request(message: IsoMessage, isEcho: Boolean): IsoMessage? =
        delegate.request(message, isEcho)

    override fun stop() = delegate.stop()

    override suspend fun receive(): IsoMessage? = delegate.receive()

    override fun close() = delegate.close()
}
