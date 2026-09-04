package com.danesh.settings.model

import androidx.annotation.StringRes
import com.danesh.settings.R

enum class PosAdminMenuAction(
    @StringRes val labelRes: Int,
) {
    Cardholder(R.string.settings_pos_menu_cardholder),
    Merchant(R.string.settings_pos_menu_merchant),
    Update(R.string.settings_pos_menu_update),
    SystemManager(R.string.settings_pos_menu_system_manager),
    Support(R.string.settings_pos_menu_support),
    Shutdown(R.string.settings_pos_menu_shutdown),
}

val posAdminMenuActions: List<PosAdminMenuAction> = PosAdminMenuAction.entries
