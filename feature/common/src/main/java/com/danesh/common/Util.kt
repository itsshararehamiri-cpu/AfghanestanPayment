package com.danesh.common

import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import saman.zamani.persiandate.PersianDate


fun getFontSize(isPaperReceipt: Boolean, context: Context): TextUnit {
    return if (isPaperReceipt) {
      Dimensions.FONT_SIZE_PAPER_RECEIPT
    } else {
        Dimensions.FONT_SIZE_RECEIPT
    }

}

fun getFontWeight(isPaperReceipt: Boolean, context: Context): FontWeight {
    return FontWeight.Medium


}

fun getFontSizeUnSuccess(isPaperReceipt: Boolean, context: Context): TextUnit {
    return Dimensions.FONT_SIZE_UNSUCCESS_PAPER_RECEIPT


}

fun getFontSizeAmount(isPaperReceipt: Boolean, context: Context): TextUnit {
    return if (isPaperReceipt) {
        Dimensions.FONT_SIZE_PAPER_RECEIPT
    } else
        Dimensions.FONT_SIZE_RECEIPT
}


fun getFontWeightUnSuccess(isPaperReceipt: Boolean, context: Context): FontWeight {
    return if (isPaperReceipt)
        FontWeight.Medium
    else FontWeight.Normal
}

fun getPersianDate(date_MMdd: String): String {
    return with(date_MMdd) {
        val cal = PersianDate()
        cal.setGrgMonth(take(2).toInt() )
        cal.setGrgDay(takeLast(2).toInt())
        cal.grgYear = cal.grgYear
        "${cal.shYear}/${if (cal.shMonth < 10) 0 else ""}${cal.shMonth}/${if (cal.shDay < 10) 0 else ""}${cal.shDay}"
    }
}