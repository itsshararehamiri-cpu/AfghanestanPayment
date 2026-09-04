package com.danesh.common.locale

import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionResultDetail

fun TerminalConfig.displayMerchantName(language: AppLanguage): String =
    MerchantNameDisplay.resolve(merchantName, englishMerchantName, language)

fun TransactionResultDetail.displayMerchantName(language: AppLanguage): String =
    MerchantNameDisplay.resolve(merchantName, englishMerchantName, language)
