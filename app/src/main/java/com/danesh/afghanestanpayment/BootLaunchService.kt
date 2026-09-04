package com.danesh.afghanestanpayment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * پس از boot با تأخیر [BootLaunchActivity] را باز می‌کند تا Launcher سیستم جلو نماند.
 */
class BootLaunchService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promoteToForeground()
        scope.launch {
            val wakeLock = acquireBootWakeLock()
            try {
                delay(BOOT_DELAY_MS)
                launchBootActivity()
                delay(RETRY_DELAY_MS)
                launchBootActivity()
            } catch (error: Exception) {
                Log.e(TAG, "boot launch failed: ${error.message}", error)
            } finally {
                wakeLock?.releaseQuietly()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun promoteToForeground() {
        ensureNotificationChannel()
        val fullScreenIntent = bootActivityPendingIntent(requestCode = 1)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.boot_launch_notification_text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(bootActivityPendingIntent(requestCode = 0))
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun bootActivityPendingIntent(requestCode: Int): PendingIntent {
        val launchIntent = Intent(this, BootLaunchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_LAUNCHED_FROM_BOOT, true)
        }
        return PendingIntent.getActivity(
            this,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.boot_launch_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.boot_launch_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun launchBootActivity() {
        startActivity(
            Intent(this, BootLaunchActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                )
                putExtra(EXTRA_LAUNCHED_FROM_BOOT, true)
            },
        )
    }

    private fun acquireBootWakeLock(): PowerManager.WakeLock? {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return null
        return powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AfghanestanPayment:BootLaunch",
        ).apply {
            setReferenceCounted(false)
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun PowerManager.WakeLock.releaseQuietly() {
        runCatching {
            if (isHeld) release()
        }
    }

    companion object {
        private const val TAG = "BootLaunchService"
        private const val CHANNEL_ID = "boot_launch"
        private const val NOTIFICATION_ID = 9001
        const val EXTRA_LAUNCHED_FROM_BOOT = "launched_from_boot"
        /** صبر تا Launcher سیستم بالا بیاید */
        private const val BOOT_DELAY_MS = 10_000L
        private const val RETRY_DELAY_MS = 5_000L
        private const val WAKE_LOCK_TIMEOUT_MS = 60_000L
    }
}
