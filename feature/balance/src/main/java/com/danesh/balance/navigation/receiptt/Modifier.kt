package com.danesh.balance.navigation.receiptt

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import com.danesh.balance.navigation.receiptt.Dimensions.BUTTON_HEIGHT
import com.danesh.balance.navigation.receiptt.Dimensions.MARGIN_BOTTOM_RECEIPT
import com.danesh.balance.navigation.receiptt.Dimensions.MARGIN_TOP_ROW_PAGER_RECEIPT
import com.danesh.balance.navigation.receiptt.Dimensions.MARGIN_TOP_ROW_RECEIPT


fun Modifier.rowReceiptModifier(isPagerReceipt: Boolean): Modifier {
    return this
        .padding(top = if (isPagerReceipt) 0.dp else 2.dp)
        .fillMaxWidth()
        .padding(top = if (isPagerReceipt) MARGIN_TOP_ROW_PAGER_RECEIPT else MARGIN_TOP_ROW_RECEIPT)
}

fun Modifier.rowReceiptWithPSPLogoModifier(isPagerReceipt: Boolean): Modifier {
    return this
        .padding(top = if (isPagerReceipt) 0.dp else 0.dp)
        .fillMaxWidth()
}

fun Modifier.containerReceiptModifier(isPaperReceipt: Boolean, context: Context): Modifier {
    val modifier = this
        .padding(
            top = if (isPaperReceipt) 0.dp else 10.dp,
            bottom = if (isPaperReceipt) 0.dp else 0.dp
        )
        .fillMaxWidth(
                 0.5f

        )
        .padding(horizontal = if (isPaperReceipt) 0.dp else 0.dp)
        .padding(bottom =  0.dp )
    return if (isPaperReceipt) modifier.background(Color.White) else modifier

}

fun Modifier.mainContainerReceipt(isPaperReceipt: Boolean): Modifier {
    return this
        .padding(horizontal = if (isPaperReceipt) 2.dp else 12.dp)
        .padding(
            top = if (isPaperReceipt) 0.dp else 20.dp,
            bottom = if (isPaperReceipt) 0.dp else 10.dp
        )
        .fillMaxWidth()
        .padding(
            top = if (isPaperReceipt) 0.dp else 15.dp,
            bottom = if (isPaperReceipt) 15.dp else 15.dp
        )

}

fun Modifier.resultLogoMoifier(isSmall: Boolean): Modifier {
    return this
        .padding(top = if (isSmall) 0.dp else 10.dp)
        .size(if (isSmall) 20.dp else 50.dp)
        .layoutId("successTickImage")
}

//@Composable
//fun Modifier.ReceiptResultModifier(isPaperReceipt: Boolean): Modifier {
//    return Modifier
//        .mainContainerReceipt(isPaperReceipt)
//        .background(
//            color = MaterialTheme.colorScheme.surface,
//            shape = RoundedCornerShape(16.dp)
//        )
//}
//
//@Composable
//fun Modifier.ReceiptResultColumnModifier(isSmall:Boolean): Modifier {
//  if(isSmall)  return Modifier
//        .padding(bottom = MARGIN_BOTTOM_RECEIPT)
//        .padding(horizontal = 3.dp)
//        .fillMaxWidth()
//        .padding(horizontal = 3.dp)
//    else  return Modifier
//        .padding(bottom = MARGIN_BOTTOM_RECEIPT)
//        .padding(horizontal = 12.dp)
//        .fillMaxWidth()
//        .padding(horizontal = 12.dp)
//}

//val TextInputModifier=Modifier
//    .padding(start = END_PADDING, end = START_PADDING, top = 16.dp)
//    .fillMaxWidth().wrapContentHeight()
//   //
val BackButtonModifier = Modifier
    .fillMaxWidth()
    .height(85.dp)
val SelectDateModifier = Modifier
    .padding(start = END_PADDING, end = START_PADDING, top = 6.dp)

@Composable
fun PrintingOptionModifier(): Modifier =
    Modifier
        .padding(start = END_PADDING, end = START_PADDING, top = 16.dp)
        .fillMaxWidth()
        .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
                )
            ),
            shape = RoundedCornerShape(16.dp)
        )
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))

//fun Modifier.PurchasePriceTextInputModifier(isSmall: Boolean): Modifier {
//    return Modifier
//        .padding(start = 10.dp, end = 10.dp, top = 0.dp)
//        .fillMaxWidth()
//}

fun Modifier.textFieldModifier(isSmall: Boolean): Modifier {
    return if (isSmall)
        this
            .fillMaxWidth()
            .height(HEIGHT_TEXT_INPUT_I5000)
            .padding(horizontal = 2.dp)
    else this
        .fillMaxWidth()
        .height(HEIGHT_TEXT_INPUT)
        .padding(horizontal = 2.dp)
}


fun Modifier.mainButtonModifier(isSmall: Boolean): Modifier =
    if (isSmall) this
        .padding(horizontal = 10.dp)
        .padding(
            bottom = 6
                .dp
        )
        .height(55.dp)
        .fillMaxWidth()
    else this
        .padding(horizontal = 10.dp)
        .padding(bottom = MARGIN_BOTTOM_MAINBUTTON)
        .height(BUTTON_HEIGHT)
        .fillMaxWidth()