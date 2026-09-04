package com.danesh.common.receipt


//@Composable
//fun ConfigurationReceiptContent(configurationResult: ConfigurationResult) {
//    val context= LocalContext.current
//    val isFarsi= LocalLanguageState.current.isFarsiSelected.value
//    Column(
//        Modifier.containerReceiptModifier(true,context)
//    ) {
//        val modifier = Modifier.rowReceiptModifier(true)
//        val textColor = Color.Black
//        if (configurationResult.merchantName.isNotEmpty()) {
//            RowReceipt(
//                modifier = modifier,
//                second = configurationResult.merchantPhone,
//                first =if(isFarsi) configurationResult.merchantName else  configurationResult.englishMerchantName,
//                textColor = textColor, isPaperReceipt = true
//            )
//        }
//
//        if (configurationResult.terminalId.isNotEmpty())
//        {
//            RowReceipt(
//                modifier = modifier,
//                first = stringResource(R.string.terminal),
//                second = configurationResult.terminalId,
//                textColor = textColor, isPaperReceipt = true
//            )
//        }
//        if (configurationResult.terminalId.isNotEmpty())
//        {
//            RowReceipt(
//                modifier = modifier,
//                first = configurationResult.date,
//                second = configurationResult.time,
//                textColor = textColor, isPaperReceipt = true
//            )
//        }
//        if (configurationResult.posCode.isNotEmpty())
//        {
//            RowReceipt(
//                modifier = modifier,
//                first = stringResource(R.string.pos_code),
//                second = configurationResult.posCode,
//                textColor = textColor, isPaperReceipt = true
//            )
//        }
//        AddPSPLog(
//            modifier = Modifier.fillMaxWidth(),
//            color = Black,
//            isPaperReceipt = true
//        )
//    }
//}