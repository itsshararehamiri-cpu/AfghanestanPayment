package com.danesh.common.receipt

import androidx.compose.runtime.Composable

/**
 * قالب‌بندی مبلغ برای نمایش در رسید — فقط برای برند همراه‌پی دقیقاً دو رقم اعشار چاپ می‌شود
 * (طبق الزام رفتار رسید همراه‌پی)؛ سایر PSPها همان قالب قبلی بدون اعشار را حفظ می‌کنند.
 */
@Composable
fun receiptAmountText(amount: String): String =
    if (LocalReceiptPspBrand.current == ReceiptPspBrand.HP) {
        amount.formatAmountTwoDecimals()
    } else {
        amount.formatAmount()
    }
