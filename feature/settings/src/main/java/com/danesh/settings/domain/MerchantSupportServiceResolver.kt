package com.danesh.settings.domain

import android.content.Context
import androidx.annotation.StringRes
import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import com.danesh.settings.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantSupportServiceResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun findSettlement(catalog: SupportCatalog): SupportMenuItem? =
        catalog.items().firstMatchingTitle(keyword(R.string.merchant_support_keyword_settlement))

    fun findChangeAccount(catalog: SupportCatalog): SupportMenuItem? =
        catalog.items().firstMatchingTitle(
            keyword(R.string.merchant_support_keyword_change),
            keyword(R.string.merchant_support_keyword_account),
        ) ?: catalog.items().firstMatchingTitle(
            keyword(R.string.merchant_support_keyword_change_account),
        )

    fun findTmUpdate(catalog: SupportCatalog): SupportMenuItem? =
        catalog.items().firstMatchingTitle(
            keyword(R.string.merchant_support_keyword_tms),
            keyword(R.string.merchant_support_keyword_update),
        ) ?: catalog.items().firstMatchingTitle(
            keyword(R.string.merchant_support_keyword_tm),
            keyword(R.string.merchant_support_keyword_refresh),
        ) ?: catalog.items().firstMatchingTitle(
            keyword(R.string.merchant_support_keyword_update),
        ) ?: catalog.items().firstMatchingTitle(
            keyword(R.string.merchant_support_keyword_tm),
        )

    private fun keyword(@StringRes resId: Int): String = context.getString(resId)

    private fun List<SupportMenuItem>.firstMatchingTitle(vararg keywords: String): SupportMenuItem? =
        firstOrNull { item ->
            val normalizedTitle = item.title.normalizePersian()
            keywords.all { keyword -> normalizedTitle.contains(keyword.normalizePersian()) }
        }

    private fun String.normalizePersian(): String =
        lowercase()
            .replace('\u200c', ' ')
            .replace(" ", "")
}
