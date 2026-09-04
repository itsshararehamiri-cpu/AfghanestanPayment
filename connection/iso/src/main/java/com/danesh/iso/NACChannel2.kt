
package com.danesh.iso
import org.jpos.core.Configuration
import org.jpos.core.ConfigurationException
import org.jpos.iso.BaseChannel
import org.jpos.iso.ISOChannel
import org.jpos.iso.ISOException
import org.jpos.iso.ISOFilter.VetoException
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOPackager
import org.jpos.iso.ISOUtil
import org.jpos.util.LogEvent
import org.jpos.util.Logger
import java.io.IOException
import java.net.ServerSocket


open class NACChannel2 : BaseChannel {

    var tpduSwap: Boolean = true

    constructor() : super()


    constructor(host: String?, port: Int, p: ISOPackager?, TPDU: ByteArray?) : super(
        host,
        port,
        p
    ) {
        this.header = TPDU
    }


    constructor(p: ISOPackager?, TPDU: ByteArray?) : super(p) {
        this.header = TPDU
    }

    constructor(p: ISOPackager?, TPDU: ByteArray?, serverSocket: ServerSocket?) : super(
        p,
        serverSocket
    ) {
        this.header = TPDU
    }

    @Throws(IOException::class)
    override fun sendMessageLength(len: Int) {
        serverOut.write(len shr 8)
        serverOut.write(len)
    }

    @Throws(IOException::class, ISOException::class)
    override fun getMessageLength(): Int {
        val b = ByteArray(2)
        serverIn.readFully(b, 0, 2)
        println("hhhhhhhhhhhhhhhh${         (((b[0].toInt()) and 0xFF) shl 8) or ((b[1].toInt()) and 0xFF)
        }")
        return (((b[0].toInt()) and 0xFF) shl 8) or ((b[1].toInt()) and 0xFF)
        // return  136
    }

    @Throws(IOException::class)
    override fun sendMessageHeader(m: ISOMsg, len: Int) {
        var h = m.getHeader()
        if (h != null) {
            if (tpduSwap && h.size == 5) {
                // swap src/dest address
                val tmp = ByteArray(2)
                System.arraycopy(h, 1, tmp, 0, 2)
                System.arraycopy(h, 3, h, 1, 2)
                System.arraycopy(tmp, 0, h, 3, 2)
            }
        } else h = header
        if (h != null) serverOut.write(h)
    }


    override fun setHeader(header: String) {
        super.setHeader(ISOUtil.str2bcd(header, false))
    }

    @Throws(ConfigurationException::class)
    override fun setConfiguration(cfg: Configuration) {
        super.setConfiguration(cfg)
        tpduSwap = cfg.getBoolean("tpdu-swap", true)
    }

    @Throws(IOException::class, ISOException::class)
    override fun send(m: ISOMsg) {
        var m = m
        val evt = LogEvent(this, "send")
        try {
            if (!isConnected()) throw ISOException("unconnected ISOChannel")
            m.setDirection(ISOMsg.OUTGOING)
            val p = getDynamicPackager(m)
            m.setPackager(p)
            m = applyOutgoingFilters(m, evt)
            evt.addMessage(m)
            m.setDirection(ISOMsg.OUTGOING) // filter may have dropped this info
            m.setPackager(p) // and could have dropped packager as well
            val b = m.pack()
            println(ISOUtil.hexString(b))
            synchronized(serverOutLock) {
                sendMessageLength(b.size + getHeaderLength(m))
                sendMessageHeader(m, b.size)
                sendMessage(b, 0, b.size)
                sendMessageTrailler(m, b)
                serverOut.flush()
            }
            cnt[TX]++
            setChanged()
            notifyObservers(m)
        } catch (e: VetoException) {
            //if a filter vets the message it was not added to the event
            evt.addMessage(m)
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
        val m = createMsg()

        synchronized(serverInLock){ // TODO:
            val len = getMessageLength()
            println("length--->$len")
            val data = ByteArray(len)
            getMessage(data, 0, len)

            m.setPackager(packager)
            m.dump(System.out,">>")
            m.unpack(data)

        }
        return m
    }

//    @Throws(IOException::class)
//    protected open fun readHeader(hLen: Int): ByteArray {
//        val header = ByteArray(hLen)
//        serverheaderIn.readFully(header, 0, hLen)
//        return

//    override fun readHeader(hLen: Int): ByteArray? {
//        serverIn.readFully(header, 0, hLen)
//        val header = ByteArray(0)
//       return header
//    }
}