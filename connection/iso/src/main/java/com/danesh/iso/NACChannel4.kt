package com.danesh.iso

import android.util.Log
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

open class NACChannel4 : BaseChannel {

    /**
     * MeganacChannel قدیمی همیشه TPDU را قبل از ارسال swap می‌کرد.
     *
     * اگر بخواهی دقیقاً همان رفتار قدیمی را داشته باشی:
     * tpduSwap = true
     */
    var tpduSwap: Boolean = true

    var lastReceivedIsoBody: ByteArray? = null
        private set

    constructor() : super()

    constructor(
        host: String?,
        port: Int,
        p: ISOPackager?,
        tpdu: ByteArray?
    ) : super(host, port, p) {
        header = SadadWireFrame.requireTpdu(tpdu)
    }

    constructor(
        p: ISOPackager?,
        tpdu: ByteArray?
    ) : super(p) {
        header = SadadWireFrame.requireTpdu(tpdu)
    }

    constructor(
        p: ISOPackager?,
        tpdu: ByteArray?,
        serverSocket: ServerSocket?
    ) : super(p, serverSocket) {
        header = SadadWireFrame.requireTpdu(tpdu)
    }

    /**
     * SADAD TPDU = 5 bytes
     */
    override fun getHeaderLength(): Int {
        return SadadWireFrame.TPDU_BYTES
    }

    override fun getHeaderLength(m: ISOMsg): Int {
        return SadadWireFrame.TPDU_BYTES
    }

    /**
     * مهم:
     *
     * SADAD/MeganacChannel در مسیر قدیمی length دو بایتی
     * روی wire نمی‌فرستاد.
     *
     * بنابراین عمداً هیچ چیزی نوشته نمی‌شود.
     */
    @Throws(IOException::class)
    override fun sendMessageLength(len: Int) {
        // NO-OP
    }

    /**
     * ارسال TPDU دقیقاً مشابه MeganacChannel قدیمی.
     *
     * ترتیب:
     *
     * ISO message
     *      ↓
     * BaseChannel.send()
     *      ↓
     * sendMessageLength()   -> nothing
     *      ↓
     * sendMessageHeader()   -> 5-byte TPDU
     *      ↓
     * sendMessage()         -> ISO body
     *
     * نتیجه روی wire:
     *
     * [5-byte TPDU][ISO body]
     */
    @Throws(IOException::class)
    override fun sendMessageHeader(m: ISOMsg, len: Int) {

        var h = m.header

        if (h == null) {
            h = header
        }

        h = SadadWireFrame.requireTpdu(h).copyOf()

        if (tpduSwap) {
            val tmp = ByteArray(2)

            System.arraycopy(
                h,
                1,
                tmp,
                0,
                2
            )

            System.arraycopy(
                h,
                3,
                h,
                1,
                2
            )

            System.arraycopy(
                tmp,
                0,
                h,
                3,
                2
            )
        }

        Log.d(
            TAG,
            "SEND TPDU=${ISOUtil.hexString(h)}"
        )

        serverOut.write(h)
    }
    @Throws(IOException::class, ISOException::class)
    override fun send(m: ISOMsg) {

        Log.d(TAG, "========== SADAD SEND START ==========")
        Log.d(TAG, "SEND MTI=${m.mti}")
        Log.d(TAG, "SEND DE41=${m.getString(41)}")
        Log.d(TAG, "SEND DE42=${m.getString(42)}")

        if (!isConnected) {
            throw ISOException("unconnected ISOChannel")
        }

        m.direction = ISOMsg.OUTGOING

        val dynamicPackager = getDynamicPackager(m)
        m.packager = dynamicPackager
        val f48 = m.getString(48).orEmpty()

        Log.d(
            "FIELD48_DEBUG",
            """
    F48=$f48
    length=${f48.length}
    billId=${f48.take(13)}
    paymentId=${f48.drop(13)}
    allDigits=${f48.all(Char::isDigit)}
    """.trimIndent()
        )
        //super.send(m)


        val evt = LogEvent(this, "send")

        try {
            if (!this.isConnected()) {
                throw ISOException("unconnected ISOChannel")
            }
var tempM=m
            tempM.setDirection(2)
            val p = this.getDynamicPackager(tempM)
            tempM.setPackager(p)
            tempM = this.applyOutgoingFilters(tempM, evt)
            evt.addMessage(tempM)
            tempM.setDirection(2)
            tempM.setPackager(p)
            val b = tempM.pack()
            synchronized(this.serverOutLock) {
                this.sendMessageLength(b.size + this.getHeaderLength(m))
                this.sendMessageHeader(tempM, b.size)
                Log.d(TAG, "senqwddds: dddd:${ISOUtil.hexString(b)}")
                this.sendMessage(b, 0, b.size)
                this.sendMessageTrailler(tempM, b)
                this.serverOut.flush()
            }

            val var10002: Int = this.cnt[1]++
            this.setChanged()
            this.notifyObservers(m)
        } catch (e: VetoException) {
            evt.addMessage(m)
            evt.addMessage(e)
            throw e
        } catch (e: ISOException) {
            evt.addMessage(e)
            throw e
        } catch (e: IOException) {
            evt.addMessage(e)
            throw e
        } catch (e: java.lang.Exception) {
            evt.addMessage(e)
            throw ISOException("unexpected exception", e)
        } finally {
            Logger.log(evt)
        }

    }
    /**
     * Header string مانند MeganacChannel.
     */
    override fun setHeader(header: String) {
        super.setHeader(
            SadadWireFrame.requireTpdu(
                ISOUtil.str2bcd(header, false)
            )
        )
    }

    /**
     * برای receive فعلاً همان رفتار MeganacChannel قدیمی را نگه می‌داریم.
     *
     * نکته:
     * available() framing مطمئن TCP نیست و بعداً بهتر است
     * با توجه به BaseChannel نسخه پروژه دقیق‌تر پیاده‌سازی شود.
     */
    /**
     * jPOS 1.9.6 وقتی getMessageLength() برابر -1 باشد اول TPDU را می‌خواند
     * و سپس این متد را برای بدنه ISO صدا می‌زند. available() بلافاصله بعد از
     * هدر معمولاً صفر است؛ باید برای اولین بایت بلاک شود.
     */
    @Throws(IOException::class)
    override fun streamReceive(): ByteArray {
        val first = serverIn.read()
        if (first < 0) {
            return ByteArray(0)
        }
        val out = java.io.ByteArrayOutputStream()
        out.write(first)
        val buf = ByteArray(4096)
        val idleDeadline = System.currentTimeMillis() + 80
        var waitUntil = idleDeadline
        while (System.currentTimeMillis() < waitUntil) {
            val available = serverIn.available()
            if (available > 0) {
                val n = serverIn.read(buf, 0, minOf(buf.size, available))
                if (n <= 0) break
                out.write(buf, 0, n)
                waitUntil = System.currentTimeMillis() + 80
            } else {
                try {
                    Thread.sleep(5)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
        val body = out.toByteArray()
        Log.d(TAG, "RECEIVE ISO BODY HEX=${ISOUtil.hexString(body)}")
        return body
    }

    /**
     * receive را به BaseChannel می‌سپاریم.
     *
     * چون دیگر getMessageLength() را برای خواندن
     * 2-byte length override نمی‌کنیم.
     */
    @Throws(IOException::class, ISOException::class)
    override fun receive(): ISOMsg {
        try {

            Log.d(TAG, "========== SADAD RECEIVE START ==========")

            Log.d(
                TAG,
                "RECEIVE connected=$isConnected " +
                        "closed=${socket?.isClosed} " +
                        "remote=${socket?.remoteSocketAddress} " +
                        "available=${serverIn.available()}"
            )

            val message = super.receive()

            lastReceivedIsoBody = try {
                message.pack()
            } catch (_: Exception) {
                null
            }

            Log.d(
                TAG,
                "RECEIVE MTI=${message.mti}"
            )

            Log.d(
                TAG,
                "RECEIVE TPDU=${message.header?.let {
                    ISOUtil.hexString(it)
                }}"
            )

            Log.d(
                TAG,
                "========== SADAD xRECEIVE END ==========${ISOUtil.hexString(lastReceivedIsoBody)}"
            )

            return message

        } catch (e: ISOException) {
            throw e
        } catch (e: IOException) {
            throw e
        }
    }

    /**
     * send سفارشی قبلی را حذف کردیم.
     *
     * BaseChannel خودش:
     *
     * pack()
     * sendMessageLength()
     * sendMessageHeader()
     * sendMessage()
     *
     * را انجام می‌دهد.
     *
     * بنابراین اینجا فقط برای log کردن می‌توانیم
     * super.send() را صدا بزنیم.
     */
//    @Throws(IOException::class, ISOException::class)
//    override fun send(m: ISOMsg) {
//
//        Log.d(
//            TAG,
//            "========== SADAD SEND START =========="
//        )
//
//        Log.d(
//            TAG,
//            "SEND MTI=${m.mti}"
//        )
//
//        Log.d(
//            TAG,
//            "SEND DE41=${m.getString(41)}"
//        )
//
//        Log.d(
//            TAG,
//            "SEND DE42=${m.getString(42)}"
//        )
//        var message = m
//        Log.d(TAG, "send: ")
//        if (!isConnected) throw ISOException("unconnected ISOChannel")
//        m.direction = ISOMsg.OUTGOING
//        val dynamicPackager = getDynamicPackager(m)
//        m.packager = dynamicPackager
////        message = applyOutgoingFilters(message, evt)
////        evt.addMessage(m)
//        m.direction = ISOMsg.OUTGOING
//        m.packager = dynamicPackager
//
//        val isoBody = m.pack()
//        m.dump(System.out, "yfytyt>")
//        Log.d(TAG, "send: ddddddddddd->${ISOUtil.hexString(isoBody)}")
//        super.send(isoBody)
//
//        Log.d(
//            TAG,
//            "========== SADAD SEND END =========="
//        )
//    }

    private companion object {

        private const val TAG = "SadadChannel"
    }
}