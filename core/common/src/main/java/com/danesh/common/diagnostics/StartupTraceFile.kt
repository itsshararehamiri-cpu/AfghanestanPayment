package com.danesh.common.diagnostics

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * ردپای مراحل LOGON و INIT برای عیب‌یابی روی دستگاه.
 * فایل در پوشهٔ Download با نام [FILE_NAME] نوشته می‌شود.
 * مقدار کلید، PIN، PAN و Track2 را ننویسید.
 */
object StartupTraceFile {
    const val FILE_NAME = "sadad-logon-init.txt"
    private const val TAG = "StartupTrace"
    private const val MAX_BYTES = 1_000_000L

    private val lock = Any()
    private val depth = AtomicInteger(0)
    @Volatile private var appContext: Context? = null
    @Volatile private var announced = false

    val isActive: Boolean get() = depth.get() > 0

    fun install(context: Context) {
        appContext = context.applicationContext
        line("APP", "trace ready — Download/$FILE_NAME")
    }

    fun begin(name: String) {
        depth.incrementAndGet()
        line("SESSION", "BEGIN $name")
    }

    fun end(name: String) {
        line("SESSION", "END $name")
        depth.updateAndGet { current -> (current - 1).coerceAtLeast(0) }
    }

    fun line(stage: String, detail: String = "") {
        runCatching {
            val text = format(stage, detail)
            Log.i(TAG, text)
            write(text + "\n")
        }
    }

    fun error(stage: String, throwable: Throwable) {
        val stack = runCatching { Log.getStackTraceString(throwable) }.getOrElse {
            throwable.stackTraceToString()
        }
        line(stage, "${throwable.javaClass.simpleName}: ${throwable.message}\n$stack")
    }

    private fun format(stage: String, detail: String): String {
        val ts = TIMESTAMP.format(Date())
        return if (detail.isBlank()) "$ts | $stage" else "$ts | $stage | $detail"
    }

    private fun write(text: String) {
        synchronized(lock) {
            val context = appContext
            val wrotePublic = context != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                runCatching { appendMediaStore(context, text) }
                    .onFailure { fallback ->
                        Log.w(TAG, "MediaStore append failed: ${fallback.message}")
                    }
                    .isSuccess
            appendFiles(text, includePublic = !wrotePublic)
        }
    }

    private fun appendFiles(text: String, includePublic: Boolean) {
        val targets = mutableListOf<File>()
        if (includePublic) {
            runCatching {
                targets += File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    FILE_NAME,
                )
            }
            targets += File("/sdcard/Download", FILE_NAME)
        }
        appContext?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let { dir ->
            targets += File(dir, FILE_NAME)
        }
        targets.distinctBy { it.absolutePath }.forEach { file ->
            runCatching {
                file.parentFile?.mkdirs()
                if (file.exists() && file.length() > MAX_BYTES) {
                    file.writeText("")
                }
                file.appendText(text)
            }
        }
        if (!announced) {
            announced = true
            Log.i(TAG, "writing $FILE_NAME under Download and app external files")
        }
    }

    private fun appendMediaStore(context: Context, text: String) {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val relative = Environment.DIRECTORY_DOWNLOADS + "/"
        val uri = resolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.SIZE),
            "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
            arrayOf(FILE_NAME, relative),
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val id = cursor.getLong(0)
            val size = cursor.getLong(1)
            val item = ContentUris.withAppendedId(collection, id)
            if (size > MAX_BYTES) {
                resolver.openOutputStream(item, "wt")?.use { }
            }
            item
        } ?: resolver.insert(
            collection,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relative)
            },
        ) ?: return
        resolver.openOutputStream(uri, "wa")?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
            stream.flush()
        }
    }

    private val TIMESTAMP = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
}
