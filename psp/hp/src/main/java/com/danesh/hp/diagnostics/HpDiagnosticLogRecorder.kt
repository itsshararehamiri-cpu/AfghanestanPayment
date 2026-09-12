package com.danesh.hp.diagnostics

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "HpDiagnosticLog"

/**
 * لاگ تشخیصی گواهی/پشتیبانی همراه‌پی — فقط جهت پیام، MTI، شناسه‌های همبستگی (STAN/RRN)،
 * نتیجهٔ انتقال، DE39 و نتیجهٔ Reverse را می‌نویسد؛ هرگز PAN، Track 2 یا PIN block دریافت نمی‌کند
 * (نوع [DiagnosticRecord] عمداً این فیلدها را ندارد)، پس نیازی به ماسک‌کردن اینجا نیست.
 *
 * فایل فعال در حافظهٔ خاص اپ (`filesDir`) نوشته می‌شود، نه Logcat یا محل عمومی؛ [rotate] هر ساعت
 * آن را به یک فایل بایگانی‌شده منتقل و بافر فعال را خالی می‌کند تا حجم آن نامحدود رشد نکند
 * (به‌کارگیری در [HpDiagnosticLogRotationTask]).
 */
@Singleton
class HpDiagnosticLogRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val diagnosticsDir: File
        get() = File(context.filesDir, "diagnostics").apply { mkdirs() }

    private val activeFile: File
        get() = File(diagnosticsDir, ACTIVE_FILE_NAME)

    fun record(entry: DiagnosticRecord) {
        scope.launch {
            val line = format(entry)
            mutex.withLock {
                runCatching { activeFile.appendText(line + "\n") }
                    .onFailure { Log.w(TAG, "failed to append diagnostic record: ${it.message}") }
            }
        }
    }

    /** فایل فعال را با برچسب زمانی بایگانی می‌کند، بافر فعال را خالی و بایگانی‌های قدیمی را حذف می‌کند. */
    suspend fun rotate(maxArchives: Int = MAX_ARCHIVES) {
        mutex.withLock {
            val active = activeFile
            if (!active.exists() || active.length() == 0L) return
            val archiveDir = File(diagnosticsDir, "archive").apply { mkdirs() }
            val archived = File(archiveDir, "pos-diagnostics-${ARCHIVE_STAMP_FORMAT.format(Date())}.log")
            runCatching {
                active.copyTo(archived, overwrite = true)
                active.writeText("")
            }.onFailure { Log.w(TAG, "failed to rotate diagnostic log: ${it.message}") }
            pruneArchives(archiveDir, maxArchives)
        }
    }

    private fun pruneArchives(archiveDir: File, keep: Int) {
        val files = archiveDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        files.drop(keep).forEach { it.delete() }
    }

    private fun format(entry: DiagnosticRecord): String {
        val ts = TIMESTAMP_FORMAT.format(Date(entry.timestampMillis))
        val fields = buildList {
            add("ts=$ts")
            add("dir=${entry.direction}")
            add("mti=${entry.mti}")
            add("corr=${entry.correlationId}")
            add("transport=${entry.transportOutcome}")
            if (entry.de39.isNotBlank()) add("de39=${entry.de39}")
            if (entry.reversalOutcome.isNotBlank()) add("reversal=${entry.reversalOutcome}")
        }
        return fields.joinToString(separator = "|")
    }

    companion object {
        private const val ACTIVE_FILE_NAME = "pos-diagnostics.log"
        private const val MAX_ARCHIVES = 24
        private val TIMESTAMP_FORMAT =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        private val ARCHIVE_STAMP_FORMAT =
            SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
    }
}
