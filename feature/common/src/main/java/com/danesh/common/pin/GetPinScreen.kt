package com.danesh.common.pin

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.PasswordScreen

private const val TAG = "BalanceFlow"

@Composable
fun GetPinScreen(
    viewModel: GetPinContract,
    track2: String,
    pan: String,
    onBackClick: () -> Unit,
    onPinComplete: (String) -> Unit,
    onTimeout: () -> Unit,
    onSuccessResult: (String) -> Unit,
    onErrorResult: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTransactionProcessing = uiState.status == GetPinStatus.Processing
    var showCommunicationErrorDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = !showCommunicationErrorDialog) {
        onBackClick()
    }
    val exitToMenu: () -> Unit = {
        if (!showCommunicationErrorDialog) {
            viewModel.clearCardData()
            onBackClick()
        }
    }

    LaunchedEffect(track2) {
        if (track2.isNotEmpty()) {
            viewModel.setCardData(track2, pan)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startPinEntry()
    }

    BackHandler(enabled = !isTransactionProcessing && !showCommunicationErrorDialog) {
        exitToMenu()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GetPinEvent.PinBlockReady -> {
                    Log.d(TAG, "GetPinScreen | PinBlockReady")
                    onPinComplete(event.pinBlock)
                }
                is GetPinEvent.TransactionSuccess -> {
                    Log.d(TAG, "GetPinScreen | TransactionSuccess | responseLen=${event.response.length}")
                    onSuccessResult(event.response)
                }
                is GetPinEvent.TransactionFailure -> {
                    val isCommunication = isCommunicationTransactionFailure(event.response)
                    Log.d(TAG, "GetPinScreen | TransactionFailure | isCommunication=$isCommunication | responseLen=${event.response.length}")
                    if (isCommunication) {
                        showCommunicationErrorDialog = true
                    } else {
                        onErrorResult(event.response)
                    }
                }
                GetPinEvent.Timeout -> {
                    Log.d(TAG, "GetPinScreen | Timeout | dialogShowing=$showCommunicationErrorDialog")
                    if (!showCommunicationErrorDialog) {
                        viewModel.clearCardData()
                        onTimeout()
                    }
                }

                GetPinEvent.Cancelled -> {
                    Log.d(TAG, "GetPinScreen | Cancelled | dialogShowing=$showCommunicationErrorDialog")
                    if (!showCommunicationErrorDialog) {
                        exitToMenu()
                    }
                }
            }
        }
    }

    val hintColor = when (uiState.status) {
        GetPinStatus.Error -> Color(0xFFFF8A80)
        else -> Color(0xFF5FFBF3)
    }

    TransactionProcessingOverlay(visible = isTransactionProcessing) {
        PasswordScreen(
            errorMessage = uiState.errorMessage,
            hintColor = hintColor,
            isReadingPin = uiState.status == GetPinStatus.Reading,
            showRetryHint = uiState.status == GetPinStatus.Error,
            onBackClick = {
                if (isTransactionProcessing || showCommunicationErrorDialog) return@PasswordScreen
                viewModel.cancelPinEntry()
                exitToMenu()
            }
        )
    }

    if (showCommunicationErrorDialog) {
        TransactionCommunicationErrorDialog(
            onConfirm = {
                showCommunicationErrorDialog = false
                viewModel.clearCardData()
                onBackClick()
            },
        )
    }
}
