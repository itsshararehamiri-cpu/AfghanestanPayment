package com.danesh.core
import com.danesh.common.RawMessage
import java.io.Closeable

interface Connection<M : RawMessage> : Closeable {
    suspend fun start()
    suspend fun send(m:M)
   suspend fun connect()
    suspend fun init(ip: String,port:Int,nii: String)
    suspend fun request(message: M,isEcho: Boolean=false): M?

    fun stop()
    suspend fun receive():M?
}