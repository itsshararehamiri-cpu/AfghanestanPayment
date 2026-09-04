package com.danesh.balance.navigation.receiptt

import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit


fun getFontSize(isPaperReceipt: Boolean, context: Context): TextUnit {
    return if (isPaperReceipt) {
         Dimensions.FONT_SIZE_PAPER_RECEIPT
    } else {
        Dimensions.FONT_SIZE_RECEIPT
    }

}

fun getFontWeight(isPaperReceipt: Boolean, context: Context): FontWeight {
    return FontWeight.ExtraBold


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

