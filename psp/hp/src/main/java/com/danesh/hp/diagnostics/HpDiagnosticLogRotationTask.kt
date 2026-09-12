package com.danesh.hp.diagnostics

import com.danesh.common.startup.AppStartupTask
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * هر ساعت فایل لاگ تشخیصی فعال را بایگانی و بافر فعال را خالی می‌کند تا روی دستگاه پایانه
 * نامحدود رشد نکند (طبق الزام لاگ‌های تشخیصی برای گواهی/پشتیبانی). حلقه در پس‌زمینه اجرا
 * می‌شود تا بقیهٔ startup taskها بلاک نشوند — دقیقاً مطابق الگوی [com.danesh.engine.SafQueueScheduler].
 */
@Singleton
class HpDiagnosticLogRotationTask @Inject constructor(
    private val recorder: HpDiagnosticLogRecorder,
) : AppStartupTask {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)

    override suspend fun run() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            while (true) {
                delay(ROTATION_INTERVAL_MS)
                recorder.rotate()
            }
        }
    }

    companion object {
        private const val ROTATION_INTERVAL_MS = 60L * 60L * 1000L
    }
}
