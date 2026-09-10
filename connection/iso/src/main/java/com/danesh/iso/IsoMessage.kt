package com.danesh.iso

import com.danesh.common.RawMessage
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOPackager


interface IsoMessage : RawMessage {
    override val field11Stan: String get() = stan
    override val field37Rrn: String? get() = rrn

    var stan: String
    var dateTime: String
    var transmissionDateTime: String
    var pointOfServiceEntryMode: String
    var currency: String
    var mti: String
    var processingCode: String
    var responseCode: String
    var terminalId: String
    var track2: String
    var tt51: String
    var pinBlock: ByteArray?
    var mac: ByteArray?
    var merchantId: String
    var mcc: String

    val rrn: String?
    var pan: String
    var amount: String
    var nii: String
    var messageReasonCode: String
    var posConditionCode: String

    var securityControlInfo: String
    var additionalAmounts: String
    var field55: String
    var field56: String
    var additionalResponseData: String
    var securityControlInfoBytes: ByteArray?
    var privateUseField61: String
    var privateUseField62: String
    var privateUseField63: String

    var f72: String
    var transportData: String

    /**
     * DE43 — نام/مکان پذیرنده. فقط در پاسخ پیکربندی اختیاری پایانه (مثلاً 1314 همراه‌پی) دریافت
     * می‌شود و هرگز نباید در درخواست خروجی ارسال شود. پیاده‌سازی پیش‌فرض no-op است؛ فقط
     * PSPهایی که این فیلد را در پروتکل خود دارند (همراه‌پی) آن را override می‌کنند.
     */
    var merchantNameLocation: String
        get() = ""
        set(_) {}




    fun toIsoMessage(received: ISOMsg?)
    fun getDump(): String
    fun setField48(packer: () -> Unit)
    fun unpackField48(data: ByteArray? = null)
    fun getField48Tag(tag: String): String?
    fun setRrn(rrn: String)
    fun setSerial(serial: String)
    fun setTransactionType(type: String)
    fun setTerminalType(type: String)
    fun setCard2NNumber(account: String)
    fun setFinancialTransactionIndicator(indicator: String)
    fun setProjectCode(projectCode: String)
    fun setTicketCode(ticket: String)
    fun setField48Tag(tag: String, value: String)
    fun privateUseField62Bytes(): ByteArray?
    fun setPrivateUseField62Bytes(value: ByteArray)
    fun setPackager(packager: ISOPackager)
    fun getIsoMessage(): ISOMsg
    fun getFieldByTag(tag: String): String
}

fun IsoMessage.requireBp(): BpIsoMessage =
    this as? BpIsoMessage ?: error("Expected BpIsoMessage but was ${this::class.simpleName}")
