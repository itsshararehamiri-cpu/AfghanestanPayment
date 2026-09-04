//package com.danesh.purchase.presentation.screens
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.util.AttributeSet
//import android.view.View
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.AbstractComposeView
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//
//@Composable
//fun ReceiptUi(content: @Composable () -> Unit, onGenerateReceipt: (Bitmap) -> Unit) {
//    val context = LocalContext.current
//    var receiptView by remember {
//        mutableStateOf(ReceiptView(context = context, content = content))
//    }
//    var isInit by remember {
//        mutableStateOf(false)
//    }
//    AndroidView(
//        modifier = Modifier.wrapContentHeight(unbounded = true),
//        factory = {
//            ReceiptView(context = it, content = content).apply {
//                View.post {
//                    receiptView = this
//                    isInit = true
//                }
//            }
//        })
//
//    if (isInit) {
//        onGenerateReceipt(generateBitmap(receiptView))
//    }
//}
//
//private fun generateBitmap(view: View): Bitmap {
//    val bitmap = Bitmap.createBitmap(
//        view.width,
//        view.height,
//        Bitmap.Config.ARGB_8888
//    )
//    val canvas = Canvas(bitmap)
//    view.layout(
//        view.left,
//        view.top,
//        view.right,
//        view.bottom
//    )
//    view.draw(canvas)
//    return bitmap
//}
//
//@SuppressLint("ViewConstructor")
//class ReceiptView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    private val content: @Composable () -> Unit,
//    showView: Boolean = false
//) : AbstractComposeView(context, attrs) {
//    init {
//        View.setLayoutDirection = View.LAYOUT_DIRECTION_RTL
//        if (!showView) {
//            View.setVisibility = View.INVISIBLE
//        }
//    }
//
//    @Composable
//    override fun Content() {
//        content()
//    }
//}
//
