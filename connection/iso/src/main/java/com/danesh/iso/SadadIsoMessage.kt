package com.danesh.iso


import android.util.Log
import com.danesh.iso.field48.Field48Tlv
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOPackager
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.Date

private val CP1256 = Charset.forName("cp1256")
private const val LOG_TAG = "SadadIsoMessage"

class SadadIsoMessage(
    private val field48: Field48Tlv,
) : IsoMessage {
    private var isoMsg = ISOMsg()

    var rawPackedBody: ByteArray? = null
        private set

    fun setRawPackedBody(bytes: ByteArray) {
        rawPackedBody = bytes.copyOf()
    }

    lateinit var date: Date
        private set


    override var posConditionCode: String
        set(value) = isoMsg.set(25, value)
        get() = isoMsg.getString(25) ?: ""
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
        get() = isoMsg.getBytes(resolveMacField(isoMsg))
        set(value) {
            if (value != null) {
                isoMsg.set(resolveMacField(isoMsg), value)
            }
        }

    fun setMacPlaceholder() {
        isoMsg.set(resolveMacField(isoMsg), ByteArray(MAC_FIELD_LENGTH))
    }

    fun unsetFields(vararg fields: Int) {
        fields.forEach { field ->
            if (isoMsg.hasField(field)) {
                isoMsg.unset(field)
            }
        }
    }

    override var merchantId: String
        set(value) = isoMsg.set(42, value)
        get() = isoMsg.getString(42) ?: ""
    override var mcc: String
        set(value) = isoMsg.set(26, value)
        get() = isoMsg.getString(26) ?: ""

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

    override fun toIsoMessage(received: ISOMsg?) {
        if (received == null) return
        isoMsg = ISOMsg()
        received.packager?.let { isoMsg.setPackager(it) }
        for (field in 0..received.maxField) {
            if (received.hasField(field)) {
                try {
                    isoMsg.set(received.getComponent(field))
                } catch (e: ISOException) {
                    Log.w("BpIsoMessage", "copy field $field failed: ${e.message}")
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
        packer()
        isoMsg.set(48, field48.packText())
    }

    override fun unpackField48(data: ByteArray?) {
        when {
            data != null -> field48.unpack(data)
            else -> {
                val bytes = isoMsg.getBytes(48) ?: return
                if (bytes.isEmpty()) return
                field48.unpack(bytes)
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


    override fun setPackager(packager: ISOPackager) {
        isoMsg.setPackager(packager)
    }
    override var transportData: String
        set(value) = isoMsg.set(59, value)
        get() = isoMsg.getString(59).orEmpty()
    override var f72: String
        get() = ""
        set(value) {}
    fun packForMac(): ByteArray {
        val clone = cloneForMac()
        val packed = clone.pack()
        check(packed.size >= MAC_FIELD_LENGTH)
        val macInput = packed.copyOf(packed.size - MAC_FIELD_LENGTH)
        assertPrimaryBitmapBit64Set(macInput)
        return macInput
    }

    fun packIsoBody(): ByteArray = isoMsg.pack()

    fun primaryBitmapHex(macInput: ByteArray): String {
        require(macInput.size >= MTI_BYTES + PRIMARY_BITMAP_BYTES) {
            "macInput کوتاه‌تر از MTI+bitmap است: ${macInput.size}"
        }
        return macInput
            .copyOfRange(MTI_BYTES, MTI_BYTES + PRIMARY_BITMAP_BYTES)
            .joinToString("") { "%02X".format(it.toInt() and 0xFF) }
    }

    fun isPrimaryBitmapBit64Set(macInput: ByteArray): Boolean {
        if (macInput.size < MTI_BYTES + PRIMARY_BITMAP_BYTES) return false
        val lastBitmapByte = macInput[MTI_BYTES + PRIMARY_BITMAP_BYTES - 1].toInt() and 0xFF
        return (lastBitmapByte and 0x01) != 0
    }

    private fun assertPrimaryBitmapBit64Set(macInput: ByteArray) {
        check(isPrimaryBitmapBit64Set(macInput)) {
            "بیت 64 bitmap در macInput روشن نیست — bitmap=${primaryBitmapHex(macInput)}"
        }
    }

    fun getMacInputDump(): String {
        val clone = cloneForMac()
        val macField = resolveMacField(clone)
        clone.unset(macField)
        return dumpIsoMsg(clone) +
                "\n(MAC input: bitmap bit $macField set field bytes excluded)"
    }

    fun getSanitizedMacInputDump(): String {
        val clone = cloneForMac()
        val macField = resolveMacField(clone)
        clone.unset(macField)
        SENSITIVE_INIT_FIELDS
            .filter { it != macField }
            .forEach { field ->
                if (clone.hasField(field)) {
                    clone.set(field, REDACTED_VALUE)
                }
            }
        return dumpIsoMsg(clone) +
                "\n(MAC input: bitmap bit $macField set field bytes excluded)"
    }

    fun printMacInput(type: String) {
    }

    fun printMacInputSanitized(type: String) {

    }

    fun macInputForVerification(): ByteArray = packForMac()

    override fun printSanitized(type: String) {
        printEachField(type, sanitized = true)
    }

    override fun printEachField(prefix: String, sanitized: Boolean) {
        val mti = isoMsg.mti.orEmpty()
        Log.d(
            LOG_TAG,
            "$prefix<mti>$mti</mti>${if (sanitized) " sanitized=\"true\"" else ""}",
        )
        for (field in 0..isoMsg.maxField) {
            if (!isoMsg.hasField(field)) continue
            val binary = isBinaryLogField(field)
            val value = formatFieldLogValue(field, sanitized)
            val typeAttr = if (binary) " type=\"binary\"" else ""
            Log.d(
                LOG_TAG,
                "$prefix<field id=\"$field\"$typeAttr>$value</field>",
            )
        }
    }

    private fun isBinaryLogField(field: Int): Boolean =
        field == 52 || field == 62 || field == 64 || field == 128

    private fun formatFieldLogValue(field: Int, sanitized: Boolean): String {
        if (sanitized && field in SENSITIVE_INIT_FIELDS) return REDACTED_VALUE
        if (isBinaryLogField(field)) {
            return isoMsg.getBytes(field)
                ?.joinToString(separator = "") { byte -> "%02X".format(byte.toInt() and 0xFF) }
                .orEmpty()
        }
        return isoMsg.getString(field)?.takeIf { it.isNotEmpty() }
            ?: isoMsg.getBytes(field)?.let { bytes ->
                if (bytes.isNotEmpty() && bytes.all { it.toInt() and 0xFF in 0x20..0x7E }) {
                    String(bytes, Charsets.ISO_8859_1)
                } else {
                    bytes.joinToString(separator = "") { byte -> "%02X".format(byte.toInt() and 0xFF) }
                }
            }
                .orEmpty()
    }

    override fun print(type: String) {
        printEachField(type, sanitized = false)
    }

    private fun cloneForMac(): ISOMsg {
        val clone = isoMsg.clone() as ISOMsg

        if (clone.packager == null) {
            isoMsg.packager?.let { clone.setPackager(it) }
        }
        val macField = resolveMacField(clone)
        clone.set(macField, ByteArray(MAC_FIELD_LENGTH))
        return clone
    }

    private fun dumpIsoMsg(msg: ISOMsg): String {
        val baos = ByteArrayOutputStream()
        PrintStream(baos, true, "cp1256").use { ps -> msg.dump(ps, " ") }
        return baos.toString("cp1256")
    }

    override fun getFieldByTag(tag: String): String {
        return  getField48Tag(tag)?:""
    }
    override fun getIsoMessage(): ISOMsg = isoMsg

    companion object {
        const val MAC_FIELD_LENGTH = 8
        const val MTI_BYTES = 2
        const val PRIMARY_BITMAP_BYTES = 8

        fun resolveMacField(msg: ISOMsg): Int =
            if (msg.maxField > 64) 128 else 64

        private const val ISO_DUMP_TAG = "IsoDump"
        private const val REDACTED_VALUE = "***"
        private val SENSITIVE_INIT_FIELDS = intArrayOf(48, 61, 62, 64)
    }
}
