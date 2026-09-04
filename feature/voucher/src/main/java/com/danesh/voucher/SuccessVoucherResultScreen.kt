package com.danesh.voucher
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.TransactionResultDetail
import com.danesh.api.amountRials
import com.danesh.common.AddAmount
import com.danesh.common.AddMaskedPanCardIssuer
import com.danesh.common.AddMerchantIdTerminalId
import com.danesh.common.AddMerchantNamePhone
import com.danesh.common.AddRRNStan
import com.danesh.common.AddReceiptType
import com.danesh.common.AddTypeDateTime
import com.danesh.common.AddVoucherChargeMSG
import com.danesh.common.AddVoucherPin
import com.danesh.common.AddVoucherSerial
import com.danesh.common.HorizontalDivider
import com.danesh.common.ShowSuccessResult
import com.danesh.common.containerReceiptModifier
import com.danesh.common.currency.currencyLabel
import com.danesh.common.locale.titleRes
import com.danesh.common.receipt.AddPSPLog
import com.danesh.common.receipt.ElectronicDashedDivider
import com.danesh.common.receipt.ElectronicReceiptAmountRow
import com.danesh.common.receipt.ElectronicReceiptCardInfoRow
import com.danesh.common.receipt.ElectronicReceiptDateTimeRow
import com.danesh.common.receipt.ElectronicReceiptHeader
import com.danesh.common.receipt.ElectronicReceiptHostTextRow
import com.danesh.common.receipt.ElectronicReceiptMerchantNamePhoneRow
import com.danesh.common.receipt.ElectronicReceiptMerchantTerminalIdRow
import com.danesh.common.receipt.ElectronicReceiptStanRRnRow
import com.danesh.common.receipt.ElectronicReceiptTransactionTypeResultRow
import com.danesh.common.receipt.ElectronicReceiptVoucherChargeMethodRow
import com.danesh.common.receipt.ElectronicReceiptVoucherPinRow
import com.danesh.common.receipt.ElectronicReceiptVoucherSerialRow
import com.danesh.common.receipt.RESULT_AUTO_HOME_DELAY_MS
import com.danesh.common.receipt.ReceiptType
import com.danesh.common.receipt.SuccessReceiptPrintHost
import com.danesh.common.receipt.formatAmount
import com.danesh.common.receipt.receiptPan
import com.danesh.common.receipt.rememberSuccessReceiptPrintFlow
import com.danesh.common.receipt.showsVoucherChargeDetails
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier
import com.danesh.ui.R
import com.danesh.ui.theme.appScreenBackground

const val TIME_TO_FINISH_SUCCESS_RESULT = RESULT_AUTO_HOME_DELAY_MS

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessVoucherResultScreen(
    viewModel: SuccessTopUpViewModel,
    response: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    autoFinishDelayMs: Int = TIME_TO_FINISH_SUCCESS_RESULT,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.init(response)
    }
    val context = LocalContext.current
    val transactionAmountRials = uiState.result?.amountRials()
    val printFlow = rememberSuccessReceiptPrintFlow(
        transactionAmountRials = transactionAmountRials,
    )
    BackHandler {
        onBackClick()
    }

    if (uiState.result != null) {
        SuccessReceiptPrintHost(
            printFlow = printFlow,
            context = context,
            autoFinishDelayMs = autoFinishDelayMs,
            onHomeClick = onHomeClick,
            onCustomerReceiptHandled = viewModel::markCustomerReceiptForQueue,
            onPrint = { bitmap, onSuccess, onFailed ->
                viewModel.print(
                    bitmap = bitmap,
                    context = context,
                    onSuccess = onSuccess,
                    onFailed = onFailed,
                )
            },
            receiptContent = { receiptType ->
                VoucherReceipt(
                    result = uiState.result!!,
                    receiptType = receiptType,
                )
            },
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {

                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_white_arrow_to_right),
                            contentDescription = stringResource(R.string.label_back),
                            tint = Color(0XFFFFFFFF),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )


            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                ElectronicReceiptHeader(
                    stringResource(com.danesh.voucher.R.string.voucher_was_successful),
                    true
                )

                Spacer(modifier = Modifier.height(24.dp))
                if (uiState.result != null) {
                    TransactionResultDetailsCard(
                        result = uiState.result!!,
                        transactionTypeIcon = com.danesh.voucher.R.drawable.charge,// TODO:
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

}

@Composable
private fun VoucherReceipt(
    result: TransactionResultDetail,
    receiptType: ReceiptType = ReceiptType.CUSTOMER_RECEIPT,
) {
    val isPaperReceipt = true
    val context = LocalContext.current
    Column(
        modifier = Modifier.containerReceiptModifier(isPaperReceipt, context)
    ) {
        val firstColor = if (isPaperReceipt) Black else MaterialTheme.colorScheme.onSurface
        val modifierRowReceipt = Modifier.rowReceiptModifier(isPaperReceipt)
        if (isPaperReceipt) {
            AddReceiptType(
                modifier = modifierRowReceipt,
                receiptType = receiptType,
                textColor = firstColor,
            )
        }
        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptWithPSPLogoModifier(isPaperReceipt),
            merchantName = result.merchantName,
            merchantPhone = result.merchantPhone,
            englishMerchantName = result.merchantName,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        if (receiptType.showsVoucherChargeDetails()) {
            if (result.voucherSerial.isNotBlank()) {
                AddVoucherSerial(
                    modifier = Modifier.rowReceiptModifier(isPaperReceipt),
                    voucherSerial = result.voucherSerial,
                    textColor = firstColor,
                    isPaperReceipt = isPaperReceipt,
                )
            }
            if (result.voucherPin.isNotBlank()) {
                AddVoucherPin(
                    modifier = Modifier.rowReceiptModifier(isPaperReceipt),
                    voucherPin = result.voucherPin,
                    textColor = firstColor,
                    isPaperReceipt = isPaperReceipt,
                )
            }
            if (!result.voucherMethod.isNullOrBlank()) {
                AddVoucherChargeMSG(
                    modifier = Modifier.rowReceiptModifier(isPaperReceipt),
                    operatorCode = result.voucherMethod!!,
                    textColor = firstColor,
                    isPaperReceipt = isPaperReceipt,
                )
            }
        }
        AddTypeDateTime(
            modifier = modifierRowReceipt,
            type = stringResource(com.danesh.voucher.R.string.voucher_title),
            date = result.date,
            time = result.time,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        HorizontalDivider(
            isPaperReceipt = isPaperReceipt
        )
        AddMerchantIdTerminalId(
            modifier = modifierRowReceipt,
            merchantId = result.merchantId,
            terminalId = result.terminalId,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )

        AddMaskedPanCardIssuer(
            modifier = modifierRowReceipt,
            maskedPan = result.receiptPan(),
            cardIssuer = result.issuerName,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        AddRRNStan(
            modifier = modifierRowReceipt,
            rrn = result.rrn,
            stan = result.trace,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        AddAmount(
            modifier = modifierRowReceipt,
            result.amount,
            isPaperReceipt = isPaperReceipt,
            textColor = firstColor
        )
        ShowSuccessResult(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally), firstColor = firstColor
        )
        if (isPaperReceipt) {
            AddPSPLog(
                modifier = Modifier.fillMaxWidth(),
                color = firstColor,
                isPaperReceipt = true
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF001A1A, widthDp = 360, heightDp = 780)
@Composable
private fun TransactionSuccessScreenPreview() {
}


@Composable
private fun TransactionResultDetailsCard(
    result: TransactionResultDetail,
    transactionTypeIcon: Int,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
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
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        result.hostReceiptText?.takeIf { it.isNotBlank() }?.let { hostText ->
            ElectronicReceiptHostTextRow(
                text = hostText,
                secondText = result.hostReceiptTextSecond,
            )
        }
        if (result.hostReceiptText.isNullOrBlank()) {
            result.hostReceiptTextSecond?.takeIf { it.isNotBlank() }?.let { second ->
                ElectronicReceiptHostTextRow(text = second)
            }
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
        if (result.voucherSerial.isNotBlank()) {
            ElectronicReceiptVoucherSerialRow(
                voucherSerial = result.voucherSerial,
            )
        }
        if (result.voucherPin.isNotBlank()) {
            ElectronicReceiptVoucherPinRow(
                voucherPin = result.voucherPin,
            )
        }
        if (!result.voucherMethod.isNullOrBlank()) {
            ElectronicReceiptVoucherChargeMethodRow(
                operatorCode = result.voucherMethod!!,
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

        if (result.maskedPan.isNotBlank()) {
            ElectronicReceiptCardInfoRow(
                pan = result.maskedPan,
                issuer = result.issuerName,
            )
        }

        if (result.date.isNotBlank() || result.time.isNotBlank()) {
            ElectronicReceiptDateTimeRow(
                result.date,
                result.time,
            )
        }

        if (result.amount.isNotBlank()) {
            ElectronicReceiptAmountRow(
                result.amount.formatAmount(), currency = currencyLabel()
            )
        }

        ElectronicDashedDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0XFF165F73),
        )

        ElectronicReceiptStanRRnRow(
            stan = result.trace,
            rrn = result.rrn,
        )

    }
}

