package com.danesh.common.locale

object MerchantNameDisplay {
    fun resolve(
        persianName: String,
        englishName: String,
        language: AppLanguage,
    ): String = when (language) {
        AppLanguage.English -> englishName.ifBlank { persianName }
        else -> persianName.ifBlank { englishName }
    }
}
