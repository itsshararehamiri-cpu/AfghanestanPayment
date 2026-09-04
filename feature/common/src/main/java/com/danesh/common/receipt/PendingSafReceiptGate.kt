package com.danesh.common.receipt

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail

/**
 * در startup/resume: اگر رکورد SAF با رسید چاپ‌نشده باشد، فقط رسید کاغذی نمایش/چاپ
 * می‌شود و پس از آن Advice/Reverse ارسال می‌گردد.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PendingSafReceiptGate(
    viewModel: PendingSafReceiptViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val pendingReceipt by viewModel.pendingReceipt.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        pendingReceipt?.let { detail ->
            PendingSafReceiptScreen(
                detail = detail,
                onCustomerReceiptHandled = viewModel::onCustomerReceiptHandled,
                onPrint = viewModel::print,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PendingSafReceiptScreen(
    detail: TransactionResultDetail,
    onCustomerReceiptHandled: () -> Unit,
    onPrint: (
        bitmap: Bitmap,
        context: android.content.Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) -> Unit,
) {
    val context = LocalContext.current
    val printFlow = rememberSuccessReceiptPrintFlow(merchantReceiptEnabled = false)
    var receiptBitmap: Bitmap? by remember(detail.date, detail.time) { mutableStateOf(null) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White),
//        contentAlignment = Alignment.TopCenter,
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(horizontal = 8.dp, vertical = 12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            TransactionPaperReceipt(
//                result = detail,
//                receiptType = ReceiptType.CUSTOMER_RECEIPT,
//                isPaperReceipt = true,
//            )
//        }
//    }

    ReceiptUi(
        content = {
            TransactionPaperReceipt(
                result = detail,
                receiptType = printFlow.activeReceiptType,
                isPaperReceipt = true,
            )
        },
        onGenerateReceipt = { receiptBitmap = it },
        receiptKey = "${detail.date}${detail.time}",
    )

    SuccessReceiptPrintEffects(
        printFlow = printFlow,
        receiptBitmap = receiptBitmap,
        context = context,
        autoFinishDelayMs = 60_000,
        onHomeClick = { },
        onClearReceiptBitmap = { receiptBitmap = null },
        onCustomerReceiptHandled = onCustomerReceiptHandled,
        onPrint = { bitmap, onSuccess, onFailed ->
            onPrint(bitmap, context, onSuccess, onFailed)
        },
    )
}
