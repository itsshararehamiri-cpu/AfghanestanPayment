//package com.example.totanpay.common.receipt
//
//import android.content.Context
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.TextUnit
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.totanpay.common.isSmall
//import com.example.totanpay.ui.theme.Dimensions
//
//fun getFontSize(isPaperReceipt: Boolean, context: Context): TextUnit {
//    return if (isPaperReceipt) {
//        if (isSmall(context)) {
//            16.sp
//        } else Dimensions.FONT_SIZE_PAPER_RECEIPT
//    } else {
//        if(isSmall(context))
//            20.sp
//        else Dimensions.FONT_SIZE_RECEIPT
//    }
//}
//
//fun getFontWeight(isPaperReceipt: Boolean, context: Context): FontWeight {
//    return if (isPaperReceipt)
//    {
//        if (isSmall(context)) {
//            FontWeight.Bold
//        } else {
//            FontWeight.Medium
//        }
//    } else {
//        FontWeight.Normal
//    }
//}
//
//fun getFontSizeUnSuccess(isPaperReceipt: Boolean, context: Context): TextUnit {
//    return if (isPaperReceipt) {
//        if (isSmall(context)) {
//            19.sp
//        } else {
//            Dimensions.FONT_SIZE_UNSUCCESS_PAPER_RECEIPT
//        }
//    } else
//        Dimensions.FONT_SIZE_UNSUCCESS_RECEIPT
//}
//
//fun getFontSizeAmount(isPaperReceipt: Boolean, context: Context): TextUnit {
//    return if (isPaperReceipt) {
//        if (isSmall(context)) 16.sp
//        else Dimensions.FONT_SIZE_PAPER_RECEIPT
//    } else
//        Dimensions.FONT_SIZE_RECEIPT
//}
//
//
//fun getFontWeightUnSuccess(isPaperReceipt: Boolean, context: Context): FontWeight {
//    return if (isPaperReceipt) if (isSmall(context)) {
//        FontWeight.Bold
//    } else {
//        FontWeight.Medium
//    } else FontWeight.Normal
//}
//
