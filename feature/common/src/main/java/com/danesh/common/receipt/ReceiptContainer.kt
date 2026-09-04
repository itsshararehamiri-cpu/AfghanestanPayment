package com.danesh.common.receipt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danesh.ui.R
import com.danesh.ui.button.SuccessActionButtons
import com.danesh.ui.loading.Loading
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun ReceiptContainer(modifier: Modifier= Modifier
    .fillMaxSize()
    .appScreenBackground(),toolbarTitle: String,
                     showLoading:Boolean,onBackClick:()-> Unit,onHomeClick:()-> Unit,
                     onPrintReceiptClick:()-> Unit,   errorMessage: String,showContent: Boolean,
                     printReceiptLabel: String = stringResource(R.string.balance_print_receipt),
                     content: @Composable () -> Unit,
) {
    Box(
        modifier =modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(toolbarTitle) {
                onBackClick()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if(showContent)content()
                else if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF8A80),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            if(showLoading){
                Loading(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fillMaxSize().align(Alignment.CenterHorizontally))
            }
            SuccessActionButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                onHomeClick = onHomeClick,
                onPrintReceiptClick = {
                    onPrintReceiptClick()
                },
                printReceiptLabel = printReceiptLabel,
            )
        }
    }
}