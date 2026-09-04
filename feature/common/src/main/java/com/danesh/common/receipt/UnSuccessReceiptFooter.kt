package com.danesh.common.receipt

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.api.TransactionResultDetail
import com.danesh.common.R
import com.danesh.common.getFontSize
import com.danesh.common.getFontSizeUnSuccess

object UnSuccessReceiptLabels {
    fun isPaybackCode(code: String): Boolean = code.trim() == "-1"

    @Composable
    fun failureTitleLabel(responseCode: String): String {
        val code = responseCode.trim()
        return when {
            isPaybackCode(code) -> stringResource(R.string.unsuccess_result)
            code.isNotBlank() && (code.toIntOrNull() ?: 0) > 0 ->
                "${stringResource(R.string.unsuccess_result)} - $code"
            else -> stringResource(R.string.unsuccess_result)
        }
    }
}

@Composable
fun ColumnScope.UnSuccessPaperReceiptFooter(
    result: TransactionResultDetail,
    isPaperReceipt: Boolean,
    firstColor: Color,
) {
    Log.d("TAG", "UnSuccessPaperReceiptFooter: jjjsjj${result.isSuccess}")
    Log.d("TAG", "UnSuccessPaperReceiptFooter: jjjjj${result.responseCode}")
    Log.d("TAG", "UnSuccessPaperReceiptFooter: jjjjj${UnSuccessReceiptLabels.isPaybackCode(result.responseCode)}")


    if (result.isSuccess) return

    if (UnSuccessReceiptLabels.isPaybackCode(result.responseCode)) {
        UnSuccessPaybackMessages(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally),
            isPaperReceipt = isPaperReceipt,
            textColor = firstColor.copy(alpha = 0.72f),
        )
    } else {
        val context = LocalContext.current
        val title = UnSuccessReceiptLabels.failureTitleLabel(result.responseCode)

        Text(
            text =title,// UnSuccessReceiptLabels.failureTitleLabel(result.responseCode),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSizeUnSuccess(isPaperReceipt, context),
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally),
            color = firstColor.copy(alpha = 0.72f),
        )
        result.responseMessage.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .align(Alignment.CenterHorizontally),
                color = firstColor,
            )
        }
    }
}

@Composable
fun UnSuccessPaybackMessages(
    modifier: Modifier = Modifier,
    isPaperReceipt: Boolean,
    textColor: Color,
) {
    val context = LocalContext.current
    val style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = getFontSize(isPaperReceipt, context),
    )
    val supportPhone = stringResource(R.string.receipt_payback_support_phone)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        listOf(
            stringResource(R.string.receipt_payback_message_part1),
            stringResource(R.string.receipt_payback_message_part2),
            stringResource(R.string.receipt_payback_message_part3),
            stringResource(R.string.receipt_payback_message_part4_s, supportPhone),
            stringResource(R.string.receipt_payback_message_part5_s),
        ).forEach { text ->
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = style,
                color = textColor,
            )
        }
    }
}

@Composable
fun UnSuccessElectronicFailureBlock(
    result: TransactionResultDetail,
    modifier: Modifier = Modifier,
) {
    if (UnSuccessReceiptLabels.isPaybackCode(result.responseCode)) {
        UnSuccessPaybackMessages(
            modifier = modifier.fillMaxWidth(),
            isPaperReceipt = false,
            textColor = Color(0xFFF30557),
        )
    } else {
        val title = UnSuccessReceiptLabels.failureTitleLabel(result.responseCode)

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,//UnSuccessReceiptLabels.failureTitleLabel(result.responseCode),
                color = Color(0xFFF30557),
                fontSize = 15.sp,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            result.responseMessage.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFF30557),
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
