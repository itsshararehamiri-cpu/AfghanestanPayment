package com.danesh.database.storage

import android.os.Environment
import android.os.StatFs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceStorageChecker @Inject constructor() {

    fun availableBytes(): Long {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    fun isLowOnStorage(thresholdBytes: Long = LOW_STORAGE_THRESHOLD_BYTES): Boolean =
        availableBytes() < thresholdBytes

    companion object {
        const val LOW_STORAGE_THRESHOLD_BYTES = 100L * 1024 * 1024
    }
}
