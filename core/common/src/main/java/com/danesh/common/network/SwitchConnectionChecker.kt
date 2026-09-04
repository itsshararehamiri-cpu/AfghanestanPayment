package com.danesh.common.network

/** بررسی دسترسی به سوییچ از طریق Telnet (TCP به IP/Port سرور). */
interface SwitchConnectionChecker {
    suspend fun isServerReachable(): Boolean
}
