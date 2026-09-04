package com.danesh.common.result



import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.CenterAlignedTopAppBar

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton

import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.Color.Companion.Transparent

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp

import com.danesh.common.receipt.ElectronicReceiptHeader



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun TransactionResultScreen(

    modifier: Modifier = Modifier,

    isSuccess: Boolean,

    messageTransaction: String,

    onBackClick: () -> Unit,

    content: @Composable () -> Unit,

) {

    Box(

        modifier = Modifier.fillMaxSize(),

    ) {

        Column(

            modifier = Modifier

                .fillMaxSize()

                .then(modifier),

        ) {

            CenterAlignedTopAppBar(

                title = {},

                navigationIcon = {

                    IconButton(onClick = onBackClick) {

                        Icon(

                            painter = painterResource(com.danesh.common.R.drawable.ic_white_arrow_to_right),

                            contentDescription = stringResource(com.danesh.ui.R.string.label_back),

                            tint = Color.White,

                        )

                    }

                },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(

                    containerColor = Transparent,

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

                    messageTransaction,

                    isSuccess = isSuccess,

                )



                Spacer(modifier = Modifier.height(6.dp))



                content()



                Spacer(modifier = Modifier.height(24.dp))

            }

        }

    }

}

