package com.danesh.database.cleanup

import android.util.Log
import com.danesh.common.startup.AppStartupTask
import com.danesh.database.dao.TransactionReportDao
import com.danesh.database.storage.DeviceStorageChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionReportStorageCleanup @Inject constructor(
    private val reportDao: TransactionReportDao,
    private val storageChecker: DeviceStorageChecker,
) : AppStartupTask {

    override suspend fun run() {
        cleanupIfNeeded()
    }

    suspend fun cleanupIfNeeded(): Int {
        if (!storageChecker.isLowOnStorage()) return 0
        val deleted = reportDao.deleteOldest(CLEANUP_BATCH_SIZE)
        if (deleted > 0) {
            Log.i(
                TAG,
                "Low storage cleanup removed $deleted report transactions " +
                    "(available=${storageChecker.availableBytes()} bytes)",
            )
        }
        return deleted
    }

    companion object {
        private const val TAG = "ReportStorageCleanup"
        const val CLEANUP_BATCH_SIZE = 1000
    }
}
