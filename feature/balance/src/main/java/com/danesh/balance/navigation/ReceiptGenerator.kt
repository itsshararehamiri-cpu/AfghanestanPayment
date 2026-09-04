package com.danesh.balance.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ReceiptUi(content: @Composable () -> Unit, onGenerateReceipt: (Bitmap) -> Unit) {
    val onGenerateReceiptState = rememberUpdatedState(onGenerateReceipt)

    AndroidView(
        modifier = Modifier.wrapContentHeight(unbounded = true),
        factory = { ctx ->
            ReceiptView(context = ctx, content = content).apply {
                whenSized { view ->
                    generateBitmap(view)?.let { bitmap ->
                        onGenerateReceiptState.value(bitmap)
                    }
                }
            }
        },
    )
}

private fun View.whenSized(action: (View) -> Unit) {
    if (width > 0 && height > 0) {
        action(this)
        return
    }
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (width <= 0 || height <= 0) return
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action(this@whenSized)
        }
    })
}

private fun generateBitmap(view: View): Bitmap? {
    var width = view.width
    var height = view.height

    if (width <= 0 || height <= 0) {
        val displayWidth = view.resources.displayMetrics.widthPixels
        val widthSpec = View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.AT_MOST)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        width = view.measuredWidth
        height = view.measuredHeight
        if (width <= 0 || height <= 0) return null
        view.layout(0, 0, width, height)
    }

    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        view.draw(Canvas(bitmap))
    }
}

@SuppressLint("ViewConstructor")
class ReceiptView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val content: @Composable () -> Unit,
    showView: Boolean = false
) : AbstractComposeView(context, attrs) {
    init {
        layoutDirection = LAYOUT_DIRECTION_RTL
        if (!showView) {
            visibility = INVISIBLE
        }
    }

    @Composable
    override fun Content() {
        content()
    }
}
