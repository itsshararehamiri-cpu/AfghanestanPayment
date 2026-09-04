//package com.danesh.purchase.presentation.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.outlined.CreditCard
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.danesh.common.receipt.ElectronicDashedDivider
//import com.danesh.common.receipt.ElectronicReceiptDetailRow
//import com.danesh.common.receipt.PurchaseSuccessBadge
//import com.danesh.purchase.R
//import com.danesh.ui.button.SuccessActionButtons
//import com.danesh.ui.toolbar.Toolbar
//
//data class TransactionSuccessDetails(
//    val terminalId: String,
//    val cardReaderCode: String,
//    val cardInfo: String,
//    val dateTime: String,
//    val amount: String,
//    val referenceCode: String,
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TransactionSuccessScreen(
//    details: TransactionSuccessDetails,
//    onBackClick: () -> Unit = {},
//    onHomeClick: () -> Unit = {},
//    onPrintReceiptClick: () -> Unit = {},
//) {
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    Brush.verticalGradient(
//                        colors = listOf(
//                            Color(0xFF015455),   Color(0xFF012C36),
//
//                            Color(0xFF01242F),Color(0xFF011B28),
//
//                        ),
//                    ),
//                ),
//        ) {
//            Column(modifier = Modifier.fillMaxSize()) {
//                Toolbar( stringResource(R.string.balance_back),) { onBackClick()}
//
//
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .verticalScroll(rememberScrollState())
//                        .padding(horizontal = 20.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    SuccessHeader()
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    TransactionDetailsCard(details = details)
//
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                SuccessActionButtons(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 20.dp, vertical = 20.dp),
//                    onHomeClick = onHomeClick,
//                    onPrintReceiptClick = onPrintReceiptClick,
//                )
//            }
//        }
//
//}
//
//@Composable
//private fun SuccessHeader() {
//    PurchaseSuccessBadge()
//
//    Spacer(modifier = Modifier.height(16.dp))
//
//    Text(
//        text = stringResource(R.string.balance_success_message),
//        color = Color(0XFF5FFBF3),
//        fontSize = 17.sp,
//        fontWeight = FontWeight.SemiBold,
//        textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall
//    )
//}
//
//@Composable
//private fun TransactionDetailsCard(details: TransactionSuccessDetails) {
//    val cardShape = RoundedCornerShape(16.dp)
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(cardShape)
//            .background( Color(0x66002830))
//            .border(
//                width = 1.dp,
//                color = Color(0x3300E5FF),
//                shape = cardShape,
//            )
//            .padding(horizontal = 16.dp, vertical = 18.dp),
//        verticalArrangement = Arrangement.spacedBy(14.dp),
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.CreditCard,
//                    contentDescription = null,
//                    tint =Color(0XFF5FFBF3),
//                    modifier = Modifier.size(22.dp),
//                )
//                Text(
//                    text = stringResource(R.string.balance_transaction_title),
//                    color =Color(0xFFFFFFFF),
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Check,
//                    contentDescription = null,
//                    tint = Color(0xFF00FFCC),
//                    modifier = Modifier.size(16.dp),
//                )
//                Text(
//                    text = stringResource(R.string.balance_status_success),
//                    color = Color(0xFF00FFCC),
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium,
//                    style = MaterialTheme.typography.bodySmall,
//                )
//            }
//        }
//
//        ElectronicDashedDivider()
//
//        ElectronicReceiptDetailRow(
//            label = stringResource(R.string.balance_label_terminal),
//            value = details.terminalId,
//            icon = R.drawable.ic_check,
//        )
//        ElectronicReceiptDetailRow(
//            label = stringResource(R.string.balance_label_card_reader),
//            value = details.cardReaderCode,
//            icon = R.drawable.ic_card_pos,
//        )
//        ElectronicReceiptDetailRow(
//            label = stringResource(R.string.balance_label_card_info),
//            value = details.cardInfo,
//            icon = R.drawable.ic_security_card,
//        )
//        ElectronicReceiptDetailRow(
//            label = stringResource(R.string.balance_label_datetime),
//            value = details.dateTime,
//            icon = R.drawable.ic_calendar_2,
//        )
//
//        ElectronicDashedDivider()
//
//        ElectronicReceiptDetailRow(
//            label = stringResource(R.string.balance_label_amount),
//            value = details.amount,
//            icon = R.drawable.ic_money_send,
//            valueColor = Color(0xFF00FFCC),
//            valueFontWeight = FontWeight.Bold,
//        )
//        ElectronicReceiptDetailRow(
//            label = stringResource(R.string.balance_label_reference),
//            value = details.referenceCode,
//            icon = R.drawable.ic_security_card,
//            valueColor =  Color(0xFF00FFCC),
//        )
//    }
//}
//
//
//
//@Preview(showBackground = true, backgroundColor = 0xFF001A1A, widthDp = 360, heightDp = 780)
//@Composable
//private fun TransactionSuccessScreenPreview() {
//    TransactionSuccessScreen(
//        details = TransactionSuccessDetails(
//            terminalId = "165131316135515132",
//            cardReaderCode = "5165165165",
//            cardInfo = "65165013*****1351",
//            dateTime = "1405/03/03 - 18:17",
//            amount = "2,200 AFN",
//            referenceCode = "ref5165684984614651",
//        ),
//    )
//}
