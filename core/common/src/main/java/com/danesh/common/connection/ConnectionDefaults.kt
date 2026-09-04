package com.danesh.common.connection

/**
 * مقادیر پیش‌فرض اتصال به سرور PSP.
 *
 * - [HP_IP] / [HP_PORT]: همراه‌پی و PSPهای مبتنی بر آن (HP, AP, PN, Fanava)
 * - [BP_IP] / [BP_PORT]: به‌پرداخت (BP)
 *
 * کلیدهای اولیه BP در `com.danesh.bp.key.BpKeyConfig` تعریف شده‌اند.
 */
object ConnectionDefaults {
    const val HP_IP = "46.100.13.79"
    const val HP_PORT = 50503
    const val BP_IP = "212.16.73.141"
    const val BP_PORT = 8585
    const val NII = "1"

    const val IP = HP_IP
    const val PORT = HP_PORT


    const val TMS_IP = "0.0.0.0"
    const val TMS_PORT = 0
}
