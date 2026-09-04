package com.danesh.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.currency.amountWithCurrency
import com.danesh.common.presentation.viewmodel.SwipeCardEvent
import com.danesh.common.presentation.viewmodel.SwipeCardStatus
import com.danesh.common.presentation.viewmodel.SwipeCardUiState
import com.danesh.common.presentation.viewmodel.SwipeCardViewModel
import com.danesh.common.receipt.balanceTransactionFee
import com.danesh.common.receipt.formatAmount
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeCardScreen(viewModel: SwipeCardViewModel,
                    onBackClick: () -> Unit,
                    onCardRead: (String, String) -> Unit,
                    onTimeout: () -> Unit,
                    cancelReading: () -> Unit,
                    showBalanceTransactionFee: Boolean = false,
                    ) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val balanceFeeHint = if (showBalanceTransactionFee) {
        val fee = balanceTransactionFee()
        if (fee.isNotBlank()) {
            stringResource(
                R.string.balance_fee_hint,
                amountWithCurrency(fee.formatAmount()),
            )
        } else {
            null
        }
    } else {
        null
    }

    val exitToMenu: () -> Unit = {
        viewModel.clearCardData()
        onBackClick()
    }

    BackHandler {
        exitToMenu()
    }

    LaunchedEffect(Unit) {
        viewModel.startReadCard()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SwipeCardEvent.CardRead -> onCardRead(event.track2,event.pan)
                SwipeCardEvent.Timeout -> {
                    viewModel.clearCardData()
                    onTimeout()
                }
            }
        }
    }
    when (uiState.status) {
        SwipeCardStatus.Error -> {
            InvalidCardScreen(
                message = uiState.errorMessage ?: stringResource(R.string.error_invalid_card),
                onRetryClick = {
                    viewModel.retryReadCard()
                },
                onCancelClick = {
                    viewModel.cancelReading()
                    exitToMenu()
                }
            )
        }

        else -> {
            when (uiState.status) {
                SwipeCardStatus.Error -> {
                    InvalidCardScreen(
                        message = uiState.errorMessage ?: stringResource(R.string.error_invalid_card),
                        onRetryClick = {
                           viewModel. retryReadCard()
                        },
                        onCancelClick = {
                            cancelReading()
                            exitToMenu()
                        }
                    )
                }

                else -> {
                    SwipeCardContent(
                        uiState = uiState,
                        onBackClick = exitToMenu,
                        cancelReading = {
                            viewModel.cancelReading()
                        },
                        retryReadCard = {
                            viewModel.retryReadCard()
                        },
                        onCardRead = {a,b->
                            viewModel.startReadCard()
                        },
                        onTimeout = {

                        },
                        balanceFeeHint = balanceFeeHint,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeCardContent(
    uiState: SwipeCardUiState,
    onBackClick: () -> Unit,
    onCardRead: (String, String) -> Unit,
    retryReadCard: () -> Unit,
    cancelReading:()-> Unit,
    onTimeout: () -> Unit,
    balanceFeeHint: String? = null,
) {


    val hintText = when (uiState.status) {
        SwipeCardStatus.Reading -> stringResource(R.string.swipe_card_reading)
        SwipeCardStatus.Error -> uiState.errorMessage
            ?: stringResource(R.string.swipe_card_retry)

        SwipeCardStatus.Waiting -> stringResource(R.string.plz_swipe)
    }

    val hintColor = when (uiState.status) {
        SwipeCardStatus.Error -> Color(0xFFFF8A80)
        else -> Color(0XFF5FFBF3)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(stringResource(R.string.swipe_card_title)) {
                cancelReading()
                onBackClick()
            }

            Text(
                text = hintText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = hintColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            if (!balanceFeeHint.isNullOrBlank()) {
                Text(
                    text = balanceFeeHint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    color = Color(0xFFFFFFFF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (uiState.status == SwipeCardStatus.Error) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {retryReadCard() }
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.swipe_card),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .padding(vertical = 70.dp)
                        .fillMaxSize(),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                SwipeUpIndicator()
            }

//            NfcBottomPanel(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(175.dp),
//                onClick = {
//                    if (uiState.status == SwipeCardStatus.Error) {
//                     retryReadCard()
//                    }
//                },
//            )
        }
    }
}





@Preview(showBackground = true, backgroundColor = 0xFF001A1F, widthDp = 360, heightDp = 720)
@Composable
private fun SwipeCardContentPreview() {
    SwipeCardContent(uiState = SwipeCardUiState(), onCardRead = {x,y->}, onTimeout = {}, onBackClick = {},
        cancelReading = {}, retryReadCard = {},
        )
}
