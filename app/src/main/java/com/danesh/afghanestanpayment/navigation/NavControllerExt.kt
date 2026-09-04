package com.danesh.afghanestanpayment.navigation

import androidx.navigation.NavHostController

fun NavHostController.popBackStackIfAvailable(): Boolean {
    return previousBackStackEntry != null && popBackStack()
}
