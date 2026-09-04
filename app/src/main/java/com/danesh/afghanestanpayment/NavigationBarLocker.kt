package com.danesh.afghanestanpayment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danesh.core.Device
import kotlinx.coroutines.launch

/** مخفی‌سازی navigation bar دستگاه POS از طریق SDK. */
fun AppCompatActivity.lockNavigationBar(device: Device) {
    lifecycleScope.launch {
        runCatching {
            device.lockNavigationBottom(this@lockNavigationBar)
        }
    }
}
