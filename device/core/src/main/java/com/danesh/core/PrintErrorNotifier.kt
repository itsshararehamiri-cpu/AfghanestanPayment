package com.danesh.core

import java.util.concurrent.CopyOnWriteArrayList

/**
 * کانال سراسری برای نمایش خطای پرینت در UI.
 * [LoggingDevice] در صورت [Device.print] با reportErrorToUi=true خطا را اینجا ارسال می‌کند.
 */
class PrintErrorNotifier {

    private val listeners = CopyOnWriteArrayList<(String) -> Unit>()

    fun notify(message: String) {
        if (message.isBlank()) return
        listeners.forEach { listener ->
            runCatching { listener(message) }
        }
    }

    fun addListener(listener: (String) -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }
}
