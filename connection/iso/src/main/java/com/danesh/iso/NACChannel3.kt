
package com.danesh.iso

import org.jpos.core.Configuration
import org.jpos.core.ConfigurationException
import org.jpos.iso.BaseChannel
import org.jpos.iso.ISOException
import org.jpos.iso.ISOFilter.VetoException
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOPackager
import org.jpos.iso.ISOUtil
import org.jpos.util.LogEvent
import org.jpos.util.Logger
import java.io.IOException
import java.net.ServerSocket


open class NACChannel3 : BaseChannel {

    var tpduSwap: Boolean = false

    var lastReceivedIsoBody: ByteArray? = null
        private set

    constructor() : super()

    constructor(host: String?, port: Int, p: ISOPackager?, tpdu: ByteArray?) : super(host, port, p) {
        header = BpWireFrame.requireTpdu(tpdu)
    }

    constructor(p: ISOPackager?, tpdu: ByteArray?) : super(p) {
        header = BpWireFrame.requireTpdu(tpdu)
    }

    constructor(p: ISOPackager?, tpdu: ByteArray?, serverSocket: ServerSocket?) : super(p, serverSocket) {
        header = BpWireFrame.requireTpdu(tpdu)
    }

    override fun getHeaderLength(): Int = BpWireFrame.TPDU_BYTES

    override fun getHeaderLength(m: ISOMsg): Int = BpWireFrame.TPDU_BYTES

    @Throws(IOException::class)
    override fun sendMessageLength(len: Int) {
        serverOut.write(BpWireFrame.encodeLength(len))
    }

    @Throws(IOException::class, ISOException::class)
    override fun getMessageLength(): Int {
        val lengthBytes = ByteArray(BpWireFrame.LENGTH_BYTES)
        serverIn.readFully(lengthBytes, 0, BpWireFrame.LENGTH_BYTES)
        return BpWireFrame.decodeLength(lengthBytes)
    }

    @Throws(IOException::class)
    override fun sendMessageHeader(m: ISOMsg, len: Int) {
        serverOut.write(resolveOutboundHeader(m))
    }

    override fun setHeader(header: String) {
        super.setHeader(BpWireFrame.requireTpdu(ISOUtil.str2bcd(header, false)))
    }

    @Throws(ConfigurationException::class)
    override fun setConfiguration(cfg: Configuration) {
        super.setConfiguration(cfg)
        tpduSwap = cfg.getBoolean("tpdu-swap", false)
    }

    @Throws(IOException::class, ISOException::class)
    override fun send(m: ISOMsg) {
        val wire = m.pack()
        var message = m
        val evt = LogEvent(this, "send")
        try {
            if (!isConnected) throw ISOException("unconnected ISOChannel")
            message.direction = ISOMsg.OUTGOING
            val dynamicPackager = getDynamicPackager(message)
            message.packager = dynamicPackager
            message = applyOutgoingFilters(message, evt)
            evt.addMessage(message)
            message.direction = ISOMsg.OUTGOING
            message.packager = dynamicPackager

            val isoBody = message.pack()
            message.dump(System.out, "yfytyt>")

            val tpdu = resolveOutboundHeader(message)
            val rawFrame = BpWireFrame.buildFrame(tpdu, isoBody)
            if (!isInitWireMti(message.mti)) {
            }

            synchronized(serverOutLock) {
                serverOut.write(rawFrame)
                serverOut.flush()
            }

            cnt[TX]++
            setChanged()
            notifyObservers(message)
        } catch (e: VetoException) {
            evt.addMessage(message)
            evt.addMessage(e)
            throw e
        } catch (e: ISOException) {
            evt.addMessage(e)
            throw e
        } catch (e: IOException) {
            evt.addMessage(e)
            throw e
        } catch (e: Exception) {
            evt.addMessage(e)
            throw ISOException("unexpected exception", e)
        } finally {
            Logger.log(evt)
        }
    }

    @Throws(IOException::class, ISOException::class)
    override fun receive(): ISOMsg {
        val message = createMsg()
        synchronized(serverInLock) {
            val lengthBytes = ByteArray(BpWireFrame.LENGTH_BYTES)
             serverIn.readFully(lengthBytes, 0, BpWireFrame.LENGTH_BYTES)
            val payloadLength = BpWireFrame.decodeLength(lengthBytes)

            val payload = ByteArray(payloadLength)
            getMessage(payload, 0, payloadLength)

            val rawFrame = ByteArray(BpWireFrame.LENGTH_BYTES + payloadLength)
            System.arraycopy(lengthBytes, 0, rawFrame, 0, BpWireFrame.LENGTH_BYTES)
            System.arraycopy(payload, 0, rawFrame, BpWireFrame.LENGTH_BYTES, payloadLength)
            val parsed = BpWireFrame.parsePayloadAfterLength(payload)
            lastReceivedIsoBody = parsed.isoBody.copyOf()
            message.packager = packager
            message.header = BpWireFrame.requireTpdu(parsed.tpdu)
            message.unpack(parsed.isoBody)
            if (!isInitWireMti(message.mti)) {

            } else {
            }
        }
       return message

    }

    private fun resolveOutboundHeader(message: ISOMsg): ByteArray {
        var headerBytes = message.header
        if (headerBytes != null) {
            headerBytes = headerBytes.copyOf()
            if (tpduSwap && headerBytes.size == BpWireFrame.TPDU_BYTES) {
                val tmp = ByteArray(2)
                System.arraycopy(headerBytes, 1, tmp, 0, 2)
                System.arraycopy(headerBytes, 3, headerBytes, 1, 2)
                System.arraycopy(tmp, 0, headerBytes, 3, 2)
            }
        } else {
            headerBytes = header
        }
        return BpWireFrame.requireTpdu(headerBytes)
    }

    private companion object {
        private const val TAG = "BpChannel"

        private fun isInitWireMti(mti: String?): Boolean = mti == "0800" || mti == "0810"

        private fun logInitWireFrame(direction: String, length: Int, mti: String?): String =
            if (mti != null && isInitWireMti(mti)) {
                "BP $direction init frame len=$length mti=$mti (payload omitted — sensitive F61/F62)"
            } else if (mti == null) {
                "BP $direction frame len=$length (init response — payload omitted — sensitive F61/F62)"
            } else {
                "BP $direction frame len=$length mti=$mti"
            }
    }
}
