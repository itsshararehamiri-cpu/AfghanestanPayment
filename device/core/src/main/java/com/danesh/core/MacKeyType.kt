package com.danesh.core

/**
 * منبع کلید MAC روی PED (الگوریتم X9.19).
 * SDK Centerm/PAX بعد از level2 load همیشه WORK_KEY روی makId استفاده می‌کند.
 */
enum class MacKeyType {
    MASTER,
    WORK,
}
