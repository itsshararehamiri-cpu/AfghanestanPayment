package com.danesh.common.receipt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.viewinterop.AndroidView
import com.danesh.common.locale.LocalReceiptCalendarStyle

@Composable
fun ReceiptUi(
    content: @Composable () -> Unit,
    receiptKey: Any? = null,
    onGenerateReceipt: (Bitmap) -> Unit,
) {
    val onGenerateReceiptState = rememberUpdatedState(onGenerateReceipt)
    val contentState = rememberUpdatedState(content)
    val calendarStyle = LocalReceiptCalendarStyle.current
    val receiptContent: @Composable () -> Unit = {
        CompositionLocalProvider(LocalReceiptCalendarStyle provides calendarStyle) {
            contentState.value.invoke()
        }
    }

    key(receiptKey) {
        AndroidView(
            modifier = Modifier.wrapContentHeight(unbounded = true),
            factory = { ctx ->
                Log.d("BalanceFlow", "ReceiptUi | factory called")
                ReceiptView(context = ctx).apply {
                    setContent { receiptContent() }
                    whenSized { view ->
                        Log.d("BalanceFlow", "ReceiptUi | whenSized | attached=${view.isAttachedToWindow} | w=${view.width} h=${view.height}")
                        if (!view.isAttachedToWindow) return@whenSized
                        generateBitmap(view)?.let { bitmap ->
                            Log.d("BalanceFlow", "ReceiptUi | bitmap generated | ${bitmap.width}x${bitmap.height}")
                            onGenerateReceiptState.value(bitmap)
                        }
                    }
                }
            },
            update = { view ->
                Log.d("BalanceFlow", "ReceiptUi | update | attached=${view.isAttachedToWindow} | w=${view.width} h=${view.height}")
                view.setContent { receiptContent() }
                view.post {
                    if (!view.isAttachedToWindow) {
                        Log.d("BalanceFlow", "ReceiptUi | update post | NOT attached, skip")
                        return@post
                    }
                    generateBitmap(view)?.let { bitmap ->
                        Log.d("BalanceFlow", "ReceiptUi | update post | bitmap generated | ${bitmap.width}x${bitmap.height}")
                        onGenerateReceiptState.value(bitmap)
                    }
                }
            },
        )
    }
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
    showView: Boolean = false,
) : AbstractComposeView(context, attrs) {
    private var content: @Composable () -> Unit = {}

    init {
        layoutDirection = LAYOUT_DIRECTION_RTL
        if (!showView) {
            visibility = INVISIBLE
        }
    }

    fun setContent(content: @Composable () -> Unit) {
        this.content = content
        if (isAttachedToWindow) {
            disposeComposition()
            createComposition()
            requestLayout()
        }
    }

    @Composable
    override fun Content() {
        content()
    }
}
