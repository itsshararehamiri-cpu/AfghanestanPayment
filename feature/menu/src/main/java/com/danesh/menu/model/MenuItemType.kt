package com.danesh.menu.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.danesh.menu.R

enum class MenuItemType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    PURCHASE(R.string.menu_purchase, R.drawable.ic_purchase),
    BALANCE(R.string.menu_balance, R.drawable.ic_balance),
    BILL(R.string.menu_bill, R.drawable.ic_bill),
    TOPUP(R.string.menu_topup, R.drawable.ic_charge),
    VOUCHER(R.string.menu_voucher, R.drawable.ic_charge),

    SUPPORT(R.string.menu_support, R.drawable.ic_support),
    TRANSFER(R.string.menu_transfer, R.drawable.ic_card_to_card),
    WALLET_TO_WALLET(R.string.menu_wallet_to_wallet, R.drawable.ic_card_to_card),
    CASH_DEPOSIT(R.string.menu_cash_deposit, R.drawable.ic_cash_deposit),
    CASH_OUT(R.string.menu_cash_out, R.drawable.ic_cash_out),
    SETTINGS(R.string.menu_settings, R.drawable.ic_settings),
    REPORT(R.string.menu_report, R.drawable.ic_report),
    CHANGE_ACCOUNT(R.string.menu_change_account, R.drawable.ic_support);

    val requiresTerminalConfiguration: Boolean
        get() = this !in menuItemsAccessibleWithoutConfiguration

    val requiresNetwork: Boolean
        get() = this !in menuItemsAccessibleWithoutNetwork
}

private val menuItemsAccessibleWithoutConfiguration = setOf(
    MenuItemType.SETTINGS,
    MenuItemType.REPORT,
    MenuItemType.CHANGE_ACCOUNT,
)

private val menuItemsAccessibleWithoutNetwork = setOf(
    MenuItemType.SETTINGS,
    MenuItemType.REPORT,
    MenuItemType.CHANGE_ACCOUNT,
)

val homeMenuItems = listOf(
    MenuItemType.BILL,
    MenuItemType.BALANCE,
    MenuItemType.PURCHASE,
    MenuItemType.SUPPORT,
    MenuItemType.CASH_DEPOSIT,
    MenuItemType.CASH_OUT,
    MenuItemType.TRANSFER,
    MenuItemType.WALLET_TO_WALLET,
    MenuItemType.TOPUP,
    MenuItemType.VOUCHER,
    MenuItemType.SETTINGS,
    MenuItemType.REPORT
)

private val alwaysVisibleMenuItems = setOf(MenuItemType.SETTINGS)

fun homeMenuItemsFor(enabledFeatures: Set<String>): List<MenuItemType> {
    if (enabledFeatures.isEmpty()) return homeMenuItems
    return homeMenuItems.filter { it.name in enabledFeatures }
}

fun configurableMenuItemsFor(enabledFeatures: Set<String>): List<MenuItemType> =
    homeMenuItemsFor(enabledFeatures).filter { it !in alwaysVisibleMenuItems }

fun merchantConfigurableMenuItems(): List<MenuItemType> =
    listOf(MenuItemType.CHANGE_ACCOUNT)

fun visibleHomeMenuItems(
    enabledFeatures: Set<String>,
    disabledFeatures: Set<String>,
): List<MenuItemType> =
    homeMenuItemsFor(enabledFeatures).filter { item ->
        item in alwaysVisibleMenuItems || item.name !in disabledFeatures
    }
