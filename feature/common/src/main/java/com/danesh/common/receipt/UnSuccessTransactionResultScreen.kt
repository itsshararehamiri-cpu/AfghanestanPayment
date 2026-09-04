package com.danesh.common.receipt

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.danesh.api.TransactionResultDetail
import com.danesh.common.result.TransactionResultScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnSuccessTransactionResultScreen(
    result: TransactionResultDetail?,
    errorInPrint: String,
    failureTitle: String,
    transactionTypeIcon: Int,
    onClearPrintError: () -> Unit,
    onPrintFailed: (String) -> Unit,
    onPrint: (
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    autoFinishDelayMs: Int = RESULT_AUTO_HOME_DELAY_MS,
    showBalanceOnCard: Boolean = false,
) {
    val context = LocalContext.current
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var printed by remember { mutableStateOf(false) }
    Log.d("TAG", "UnSuccessTransactionResultScreen: jjjj$result")
    BackHandler { onHomeClick() }

    result?.let { detail ->
        ReceiptUi(content = {
            TransactionPaperReceipt(
                result = detail,
                receiptType = ReceiptType.CUSTOMER_RECEIPT,
                isPaperReceipt = true,
            )
        }) { bitmap ->
            receiptBitmap = bitmap
        }
    }
    val bitmapToPrint = receiptBitmap
    LaunchedEffect(bitmapToPrint) {
        if (bitmapToPrint != null && !printed) {
            printed = true
            onPrint(
                bitmapToPrint,
                context,
                { Log.d("BalanceFlow", "UnSuccessResult | print SUCCESS") },// TODO:
                { err ->
                    onPrintFailed(err)
                },
            )
        }
    }

    CountdownEffect(autoFinishDelayMs) {
        onHomeClick()
    }

    TransactionResultScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0XFF015455),
                        Color(0XFF012C36),
                        Color(0XFF01242F),
                        Color(0XFF011B28),
                    ),
                ),
            ),
        messageTransaction = failureTitle,
        isSuccess = false,
        onBackClick = {
            onBackClick()
        },
    ) {
        result?.let { detail ->
            TransactionResultDetailsCard(
                result = detail,
                transactionTypeIcon = transactionTypeIcon,
                showBalance = showBalanceOnCard,
            )
        }
    }
}
