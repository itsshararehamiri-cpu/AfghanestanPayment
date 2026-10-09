package com.danesh.coupon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.coupon.R
import com.danesh.coupon.presentation.CouponInquiryResultUiState
import com.danesh.coupon.presentation.CouponInquiryResultViewModel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.button.OutlinedActionButton
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun CouponInquiryResultRoute(
    onPay: () -> Unit,
    onFinished: () -> Unit,
    viewModel: CouponInquiryResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // بازگشت = لغو استعلام (تا اعتبار کالابرگ رزرو نماند).
    BackHandler(enabled = !uiState.isCancelling) { viewModel.cancelInquiry() }
    CouponInquiryResultScreen(
        uiState = uiState,
        onPay = onPay,
        onCancel = viewModel::cancelInquiry,
        onCancelMessageDismissed = onFinished,
    )
}

@Composable
fun CouponInquiryResultScreen(
    uiState: CouponInquiryResultUiState,
    onPay: () -> Unit,
    onCancel: () -> Unit,
    onCancelMessageDismissed: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().appScreenBackground()) {
        Toolbar(title = stringResource(R.string.coupon_inquiry_result_title), onBackClick = onCancel)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Column(modifier = Modifier.couponCard(highlight = true).padding(16.dp)) {
                CouponSummaryRow(stringResource(R.string.coupon_total_amount), rials(uiState.totalAmount))
                CouponSummaryRow(stringResource(R.string.coupon_credit_amount), rials(uiState.creditAmount))
                HorizontalDivider(color = CouponCardBorder, modifier = Modifier.padding(vertical = 6.dp))
                CouponSummaryRow(
                    stringResource(R.string.coupon_cash_amount),
                    rials(uiState.cashAmount),
                    emphasize = true,
                )
                if (uiState.trackingNumber.isNotBlank()) {
                    CouponSummaryRow(stringResource(R.string.coupon_tracking_number), uiState.trackingNumber)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.lines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.coupon_cart),
                    color = AppColors.TextOnBackground,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.couponCard().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    uiState.lines.forEach { line ->
                        val name = line.product.nameFa.ifBlank { line.product.nameEn }
                        val label = if (line.product.countable) "$name × ${line.effectiveCount}" else name
                        CouponSummaryRow(label, rials(line.totalRials))
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(20.dp)) {
            if (uiState.isCancelling) {
                CircularProgressIndicator(
                    color = AppColors.Accent,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                GradientActionButton(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    text = stringResource(R.string.coupon_pay),
                    onClick = onPay,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedActionButton(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    text = stringResource(R.string.coupon_cancel_inquiry),
                    onClick = onCancel,
                )
            }
        }
    }
    uiState.cancelMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onCancelMessageDismissed,
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onCancelMessageDismissed) { Text(stringResource(R.string.coupon_ok)) }
            },
        )
    }
}
