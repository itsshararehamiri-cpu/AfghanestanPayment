package com.danesh.sadad.keycard

import android.content.Context
import android.util.Log
import com.danesh.core.SensitiveBytes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * کلیدهای پوشش کارت: TMK / MAC / DATA برای unwrap فیلد ۴۸ لاگان
 * (PIN←TMK 0x00، MAC←MAC 0x01، DATA←DATA 0x02). TMK برای نوشتن working keys روی PED هم هست.
 */
@Singleton
class SadadWrappingKeyHolder private constructor(
    private val store: SadadWrappingKeyStore?,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(SadadWrappingKeyStore(context))

    @Volatile
    private var terminalMasterKey: ByteArray? = null
    @Volatile
    private var pinKey: ByteArray? = null
    @Volatile
    private var macKey: ByteArray? = null
    @Volatile
    private var dataKey: ByteArray? = null

    fun storeFromCard(keys: SadadKeyCardMasterKeys) {
        wipeRam()
        terminalMasterKey = keys.terminalMasterKey.copyOf()
        pinKey = keys.pinKey.copyOf()
        macKey = keys.macKey.copyOf()
        dataKey = keys.dataKey.copyOf()
        persist()
    }

    fun requireTmk(): ByteArray = copyOrRestore { terminalMasterKey }
        ?: error("کلید TMK کارت برای تزریق working key به PED موجود نیست")

    fun requirePin(): ByteArray = copyOrRestore { pinKey }
        ?: error("کلید master PIN کارت برای unwrap لاگان موجود نیست")

    fun requireMac(): ByteArray = copyOrRestore { macKey }
        ?: error("کلید master MAC کارت برای unwrap لاگان موجود نیست")

    fun requireData(): ByteArray = copyOrRestore { dataKey }
        ?: error("کلید master DATA کارت برای unwrap لاگان موجود نیست")

    fun replaceWorkingPin(plainPin: ByteArray) {
        pinKey?.let { SensitiveBytes.wipe(it) }
        pinKey = plainPin.copyOf()
        persist()
    }

    fun replaceWorkingMac(plainMac: ByteArray) {
        macKey?.let { SensitiveBytes.wipe(it) }
        macKey = plainMac.copyOf()
        persist()
    }

    fun replaceWorkingData(plainData: ByteArray) {
        dataKey?.let { SensitiveBytes.wipe(it) }
        dataKey = plainData.copyOf()
        persist()
    }

    fun wipe() {
        wipeRam()
    }

    private fun copyOrRestore(getter: () -> ByteArray?): ByteArray? {
        getter()?.let { return it.copyOf() }
        restore()
        return getter()?.copyOf()
    }

    private fun persist() {
        val tmk = terminalMasterKey ?: return
        val pin = pinKey ?: return
        val mac = macKey ?: return
        val data = dataKey ?: return
        val persistStore = store ?: return
        runCatching {
            persistStore.save(
                SadadStoredWrappingKeys(
                    terminalMasterKey = tmk.copyOf(),
                    pinKey = pin.copyOf(),
                    macKey = mac.copyOf(),
                    dataKey = data.copyOf(),
                ),
            )
        }.onFailure { error ->
            Log.e(TAG, "persist wrapping keys failed", error)
        }
    }

    private fun restore() {
        val persistStore = store ?: return
        val loaded = runCatching { persistStore.load() }.onFailure { error ->
            Log.e(TAG, "restore wrapping keys failed", error)
        }.getOrNull() ?: return
        wipeRam()
        terminalMasterKey = loaded.terminalMasterKey
        pinKey = loaded.pinKey
        macKey = loaded.macKey
        dataKey = loaded.dataKey
    }

    private fun wipeRam() {
        terminalMasterKey?.let { SensitiveBytes.wipe(it) }
        pinKey?.let { SensitiveBytes.wipe(it) }
        macKey?.let { SensitiveBytes.wipe(it) }
        dataKey?.let { SensitiveBytes.wipe(it) }
        terminalMasterKey = null
        pinKey = null
        macKey = null
        dataKey = null
    }

    companion object {
        private const val TAG = "LOGON"

        fun inMemoryForTests(): SadadWrappingKeyHolder = SadadWrappingKeyHolder(null)
    }
}
