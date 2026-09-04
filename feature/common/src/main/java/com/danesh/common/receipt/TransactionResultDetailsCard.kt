package com.danesh.common.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danesh.api.TransactionResultDetail
import com.danesh.common.locale.titleRes
import com.danesh.common.receipt.formatAmount

@Composable
fun TransactionResultDetailsCard(
    result: TransactionResultDetail,
    transactionTypeIcon: Int,
    modifier: Modifier = Modifier,
    showBalance: Boolean = false,
) {
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush =
                    Brush.horizontalGradient(
                        listOf(
                            Color(0XFF002E3D),
                            Color(0XFF005562),
                            Color(0XFF002733),
                        ),
                    ),
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1F4955),
                shape = cardShape,
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val hostFirst = result.hostReceiptText?.takeIf { it.isNotBlank() }
        val hostSecond = result.hostReceiptTextSecond?.takeIf { it.isNotBlank() }
        AddPSPLog(
            modifier = Modifier.fillMaxWidth(),
            color = Black,
            isPaperReceipt = false
        )
        when {
            hostFirst != null -> ElectronicReceiptHostTextRow(
                text = hostFirst,
                secondText = hostSecond,
            )

            hostSecond != null -> ElectronicReceiptHostTextRow(text = hostSecond)
        }

        ElectronicReceiptTransactionTypeResultRow(
            type = stringResource(result.transactionType.titleRes()),
            icon = transactionTypeIcon,
            isSuccess = result.isSuccess,
        )

        if (result.merchantName.isNotBlank() || result.englishMerchantName.isNotBlank() || result.merchantPhone.isNotBlank()) {
            ElectronicReceiptMerchantNamePhoneRow(
                merchantName = result.merchantName,
                merchantPhone = result.merchantPhone,
                englishMerchantName = result.englishMerchantName,
            )
        }

        ElectronicDashedDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0XFF165F73),
        )

        ElectronicReceiptMerchantTerminalIdRow(
            result.terminalId,
            result.merchantId,
        )

        if (result.payId.isNotBlank()) {
            ElectronicReceiptDepositIdRow(depositId = result.payId)
        }

        if (result.isTransferTransaction()) {
            ElectronicTransferReceiptDetails(result)
        } else {
            val pan = result.receiptPan()
            if (pan.isNotBlank()) {
                ElectronicReceiptCardInfoRow(
                    pan = pan,
                    issuer = result.issuerName,
                )
            }
        }

        if (result.date.isNotBlank() || result.time.isNotBlank()) {
            ElectronicReceiptDateTimeRow(
                result.date,
                result.time,
            )
        }

        if (showBalance) {
            val actual = result.actualBalance?.takeIf { it.isNotBlank() }
            val available = result.availableBalance?.takeIf { it.isNotBlank() }
            if (actual != null) {
                ElectronicReceiptBalanceRow(actual.formatAmount())
            }
            if (available != null && available != actual) {
                ElectronicReceiptAvailableBalanceRow(available.formatAmount())
            } else if (actual == null && available != null) {
                ElectronicReceiptAvailableBalanceRow(available.formatAmount())
            }
            val balanceFee = balanceTransactionFee()
            if (balanceFee.isNotBlank()) {
                ElectronicReceiptBalanceFeeRow(balanceFee)
            }
        }

        ElectronicDashedDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0XFF165F73),
        )

        if (result.trace.isNotBlank() || !result.rrn.isNullOrBlank()) {
            ElectronicReceiptStanRRnRow(
                stan = result.trace,
                rrn = result.rrn,
            )
        }
        if (!result.isSuccess) {
            UnSuccessElectronicFailureBlock(
                result = result,
                modifier = Modifier.fillMaxWidth(),
            )
        }


    }
}
