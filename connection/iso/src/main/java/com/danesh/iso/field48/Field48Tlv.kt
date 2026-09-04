package com.danesh.iso.field48

interface Field48Tlv {
    fun addNode(key: String, value: String): Field48Tlv
    fun getNode(key: String): String?
    fun packText(): String
    fun pack(): ByteArray
    fun unpack(data: String)
    fun unpack(bytes: ByteArray)
    fun clear()
}
