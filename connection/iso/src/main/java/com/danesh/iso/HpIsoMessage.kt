package com.danesh.iso

import android.util.Log
import com.danesh.iso.field48.HpField48Tlv
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOPackager
import org.jpos.iso.ISOUtil
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import javax.inject.Inject

private val CP1256 = Charset.forName("cp1256")


class HpIsoMessage @Inject constructor() : IsoMessage {
    private var isoMsg = ISOMsg()
    private val field48 = HpField48Tlv()

    override var stan: String
        set(value) = isoMsg.set(11, value)
        get() = isoMsg.getString(11) ?: ""

    override var dateTime: String
        set(value) = isoMsg.set(12, value)
        get() = isoMsg.getString(12) ?: ""

    override var transmissionDateTime: String
        set(value) = isoMsg.set(7, value)
        get() = isoMsg.getString(7) ?: ""

    override var pointOfServiceEntryMode: String
        set(value) = isoMsg.set(22, value)
        get() = isoMsg.getString(22) ?: ""

    override var currency: String
        set(value) = isoMsg.set(49, value)
        get() = isoMsg.getString(49) ?: ""

    override var mti: String
        get() = isoMsg.getString(0) ?: ""
        set(value) = isoMsg.set(0, value)

    override var processingCode: String
        get() = isoMsg.getString(3) ?: ""
        set(value) = isoMsg.set(3, value)

    override var responseCode: String
        get() = isoMsg.getString(39) ?: ""
        set(value) = isoMsg.set(39, value)

    override var terminalId: String
        set(value) = isoMsg.set(41, value)
        get() = isoMsg.getString(41) ?: ""

    override var track2: String
        get() = isoMsg.getString(35) ?: ""
        set(value) = isoMsg.set(35, value)

    override var tt51: String
        get() = isoMsg.getString(51) ?: ""
        set(value) = isoMsg.set(51, value)

    override var pinBlock: ByteArray?
        get() = isoMsg.getBytes(52) ?: ByteArray(0)
        set(value) = isoMsg.set(52, value)

    override var mac: ByteArray?
        get() = isoMsg.getBytes(64)
        set(value) = isoMsg.set(64, value)

    override var merchantId: String
        set(value) = isoMsg.set(42, value)
        get() = isoMsg.getString(42) ?: ""

    override val rrn: String?
        get() = isoMsg.getString(37)

    override var pan: String
        get() = isoMsg.getString(2) ?: ""
        set(value) = isoMsg.set(2, value)

    override var amount: String
        get() = isoMsg.getString(4) ?: ""
        set(value) = isoMsg.set(4, value)

    override var nii: String
        set(value) = isoMsg.set(24, value)
        get() = isoMsg.getString(24) ?: ""

    override var messageReasonCode: String
        set(value) = isoMsg.set(25, value)
        get() = isoMsg.getString(25) ?: ""

    override var securityControlInfo: String
        set(value) = isoMsg.set(53, value)
        get() = isoMsg.getString(53) ?: ""

    override var additionalAmounts: String
        set(value) = isoMsg.set(54, value)
        get() = isoMsg.getString(54) ?: ""

    override var field55: String
        set(value) = isoMsg.set(55, value)
        get() = isoMsg.getString(55)
            ?: isoMsg.getBytes(55)?.let { String(it, CP1256) }
            ?: ""

    override var field56: String
        set(value) = isoMsg.set(56, value)
        get() = isoMsg.getString(56)
            ?: isoMsg.getBytes(56)?.let { String(it, CP1256) }
            ?: ""

    override var additionalResponseData: String
        set(value) = isoMsg.set(44, value)
        get() = isoMsg.getString(44).orEmpty()

    override var securityControlInfoBytes: ByteArray?
        get() = isoMsg.getBytes(53)
        set(value) = isoMsg.set(53, value)

    override var privateUseField61: String
        set(value) = isoMsg.set(61, value)
        get() = isoMsg.getString(61).orEmpty()

    override var privateUseField62: String
        set(value) = isoMsg.set(62, value)
        get() = isoMsg.getString(62).orEmpty()

    override fun privateUseField62Bytes(): ByteArray? = isoMsg.getBytes(62)

    override fun setPrivateUseField62Bytes(value: ByteArray) {
        isoMsg.set(62, value)
    }

    override var privateUseField63: String
        set(value) = isoMsg.set(63, value)
        get() = isoMsg.getString(63).orEmpty()


    override fun toIsoMessage(received: ISOMsg?) {
        if (received == null) return
        isoMsg = ISOMsg()
        received.packager?.let { isoMsg.setPackager(it) }
        for (field in 0..received.maxField) {
            if (received.hasField(field)) {
                try {
                    isoMsg.set(received.getComponent(field))
                } catch (e: ISOException) {
                    Log.w("HpIsoMessage", "copy field $field failed: ${e.message}")
                }
            }
        }
        unpackField48()
    }

    override fun getDump(): String {
        val baos = ByteArrayOutputStream()
        PrintStream(baos, true, "cp1256").use { ps -> isoMsg.dump(ps, " ") }
        return baos.toString("cp1256")
    }

    override fun setField48(packer: () -> Unit) {
        field48.clear()
        packer()

        val packedText = field48.packText()
        isoMsg.set(48, packedText)
    }

    override fun unpackField48(data: ByteArray?) {
        when {
            data != null -> field48.unpack(data)
            else -> {
                val text = isoMsg.getString(48)
                    ?: isoMsg.getBytes(48)?.let { String(it, CP1256) }
                    ?: return
                if (text.isEmpty()) return
                field48.unpack(text)
            }
        }
    }

    override fun getField48Tag(tag: String): String? = field48.getNode(tag)

    override fun setRrn(rrn: String) {
        isoMsg.set(37, rrn)
    }

    override fun setSerial(serial: String) {
        field48.addNode("001", serial)
    }

    override fun setTransactionType(type: String) {
        field48.addNode("002", type)
    }

    override fun setTerminalType(type: String) {
        field48.addNode("012", type)
    }

    override fun setCard2NNumber(account: String) {
        field48.addNode("021", account)
    }

    override fun setFinancialTransactionIndicator(indicator: String) {
        field48.addNode("040", indicator)
    }

    override fun setProjectCode(projectCode: String) {
        field48.addNode("007", projectCode)
    }

    override fun setTicketCode(ticket: String) {
        field48.addNode("008", ticket)
    }

    override fun setField48Tag(tag: String, value: String) {
        field48.addNode(tag, value)
    }

    override fun setPackager(packager: ISOPackager) {
        isoMsg.setPackager(packager)
    }

    override fun getIsoMessage(): ISOMsg = isoMsg

    override fun print(type: String) {
        isoMsg.dump(System.out, type)
    }
    override fun getFieldByTag(tag: String): String {
        return  getField48Tag(tag)?:""
    }
    override var f72: String
        get() = ""
        set(value) {}
}
