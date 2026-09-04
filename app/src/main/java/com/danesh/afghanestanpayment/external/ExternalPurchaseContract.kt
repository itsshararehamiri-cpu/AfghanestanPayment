package com.danesh.afghanestanpayment.external

import android.content.Context
import android.content.Intent

object ExternalPurchaseContract {
    const val ACTION = "com.danesh.afghanestanpayment.action.EXTERNAL_PURCHASE"
    const val EXTRA_AMOUNT = "amount"

    /** JSON string returned to the caller app on [android.app.Activity.RESULT_OK]. */
    const val EXTRA_RESPONSE = "purchase_response"

    fun createIntent(context: Context, amount: String): Intent =
        Intent(ACTION).setPackage(context.packageName).putExtra(EXTRA_AMOUNT, amount)
}
