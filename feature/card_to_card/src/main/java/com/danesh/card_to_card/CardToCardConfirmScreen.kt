package com.danesh.card_to_card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.card_to_card.model.CardToCardTransferDetails
import com.danesh.common.currency.currencyLabel
import com.danesh.card_to_card.ui.CardToCardConfirmCard
import com.danesh.card_to_card.ui.theme.CardToCardColors
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.AppColors
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardToCardConfirmScreen(
    details: CardToCardTransferDetails,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onConfirmClick: (CardToCardTransferDetails) -> Unit
) {
    Scaffold(

        modifier = Modifier.fillMaxSize(),

        containerColor = CardToCardColors.Background,

        topBar = {
            Toolbar(stringResource(R.string.card_to_card_title)) {
                onBackClick()
            }

        },

        ) { innerPadding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(innerPadding)

                .padding(horizontal = 24.dp),

            ) {

            Spacer(modifier = Modifier.height(8.dp))

            CardToCardConfirmCard(
                details = details
            )

            Spacer(modifier = Modifier.weight(1f))

            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.card_to_card_confirm),
                onClick = { onConfirmClick(details) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun CardToCardConfirmScreenPreview() {
    CardToCardConfirmScreen(
        details = CardToCardTransferDetails(
            recipientName = "عرفانه سلیمانی روزبهانی",
            sourceCardNumber = "5022 2915 5235 5602",
            destinationNumber = "5047 0611 4480 1300",
            amount = "10,000,000",
            currency = currencyLabel(),
        ),
        onConfirmClick = {},
        onBackClick = {},
        onEditClick = {},
    )
}
