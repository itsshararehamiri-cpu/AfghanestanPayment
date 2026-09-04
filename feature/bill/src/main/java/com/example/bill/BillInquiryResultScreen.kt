package com.example.bill

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.danesh.ui.theme.AppColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.toolbar.Toolbar

data class BillInquiryResultDetails(
    val billTypeTitle: String,
    @DrawableRes val billTypeIcon: Int = R.drawable.ic_water_drop,
    val billId: String,
    val debtAmount: String,
    val taxAmount: String,
    val paymentDeadline: String,
    val payableAmount: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillInquiryResultScreen(
    details: BillInquiryResultDetails,
    onBackClick: () -> Unit = {},
    onConfirmAndPayClick: () -> Unit = {},
) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppColors.ScreenBackground,
            topBar = {
                Toolbar( stringResource(R.string.bill_inquiry_result_title)) { onBackClick()}
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BillInquiryResultCard(details = details)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                GradientActionButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    text = stringResource(R.string.bill_confirm_and_pay),
                    onClick = onConfirmAndPayClick,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

}

@Composable
private fun BillInquiryResultCard(details: BillInquiryResultDetails) {
    val cardShape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(brush = Brush.linearGradient(listOf(
                Color(0XFF002E3D),
                Color(0XFF005562),
                        Color(0XFF002733)
            )))
            .border(
                width = 1.dp,
                color = Color(0XFF1F4955),
                shape = cardShape,
            ),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_water_drop),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(180.dp)
                .alpha(0.12f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BillInquiryCardHeader(
                billTypeTitle = details.billTypeTitle,
                billTypeIcon = details.billTypeIcon,
            )

            BillDashedDivider()

            BillDetailRow(
                label = stringResource(R.string.bill_label_bill_id),
                icon = R.drawable.ic_id,
                value = details.billId,
            )
            BillDetailRow(
                label = stringResource(R.string.bill_label_debt_amount),
                icon = R.drawable.ic_money_send,
                value = details.debtAmount,
            )
            BillDetailRow(
                label = stringResource(R.string.bill_label_tax),
                icon = R.drawable.ic_security_card,
                value = details.taxAmount,
            )
            BillDetailRow(
                label = stringResource(R.string.bill_label_payment_deadline),
                icon = R.drawable.ic_calendar_2,
                value = details.paymentDeadline,
            )
            BillDetailRow(
                label = stringResource(R.string.bill_label_payable_amount),
                icon = R.drawable.ic_payable_amount,
                value = details.payableAmount,
                valueColor = Color(0XFFFFFFFF),
                valueFontWeight = FontWeight.Bold,
                valueFontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun BillInquiryCardHeader(
    billTypeTitle: String,
    @DrawableRes billTypeIcon: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BillIconContainer(icon = billTypeIcon, size = 36.dp)
            Text(
                text = billTypeTitle,
                color = Color(0XFFFFFFFF),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun BillInquiryResultScreenPreview() {
    BillInquiryResultScreen(
        details = BillInquiryResultDetails(
            billTypeTitle = "قبض آب",
            billId = "1651313161352",
            debtAmount = "1000 AFN",
            taxAmount = "100 AFN",
            paymentDeadline = "24 May 2026",
            payableAmount = "2,200 AFN",
        ),
    )
}
