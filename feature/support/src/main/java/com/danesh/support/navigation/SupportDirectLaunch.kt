package com.danesh.support.navigation

data class SupportDirectLaunch(
    val amount: String,
    val serviceId: String,
    val title: String,
    val track2: String = "",
    val pan: String = "",
)
