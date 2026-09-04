package com.danesh.bp.support

import android.content.Context
import android.util.Log
import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import com.danesh.bp.field48.BpField48Tags
import com.danesh.iso.IsoMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpSupportMenuStore @Inject constructor(
    @ApplicationContext context: Context,
) : SupportCatalog, SupportMenuResponsePersister {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun items(): List<SupportMenuItem> =
        BpSupportMenuParser.parse(prefs.getString(KEY_TAG23, null))

    override fun persistFromResponse(response: IsoMessage) {
        response.unpackField48()
        val tag23 = response.getField48Tag(BpField48Tags.SUPPORT_TITLES_AMOUNTS)
            ?.takeIf { it.isNotBlank() }
            ?: return
        Log.d("TAG", "persistFromResponse: ddddddddddd$tag23")
        prefs.edit().putString(KEY_TAG23, tag23).apply()
    }

    fun rawTag23(): String = prefs.getString(KEY_TAG23, "").orEmpty()

    override fun clear() {
        prefs.edit().remove(KEY_TAG23).apply()
    }

    companion object {
        private const val PREFS_NAME = "bp_support_menu_prefs"
        private const val KEY_TAG23 = "field48_tag23"
    }
}
