package com.danesh.menu.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuItemVisibilityTest {

    @Test
    fun `settings stays visible when disabled in preferences`() {
        val visible = visibleHomeMenuItems(
            enabledFeatures = setOf("PURCHASE", "SETTINGS"),
            disabledFeatures = setOf("SETTINGS", "PURCHASE"),
        )

        assertEquals(listOf(MenuItemType.SETTINGS), visible)
    }

    @Test
    fun `disabled purchase is hidden from menu`() {
        val visible = visibleHomeMenuItems(
            enabledFeatures = setOf("PURCHASE", "BILL", "SETTINGS"),
            disabledFeatures = setOf("PURCHASE"),
        )

        assertFalse(visible.contains(MenuItemType.PURCHASE))
        assertTrue(visible.contains(MenuItemType.BILL))
        assertTrue(visible.contains(MenuItemType.SETTINGS))
    }

    @Test
    fun `transaction menu items require terminal configuration`() {
        listOf(
            MenuItemType.PURCHASE,
            MenuItemType.BALANCE,
            MenuItemType.BILL,
            MenuItemType.BILL_INQUIRY,
            MenuItemType.TOPUP,
            MenuItemType.VOUCHER,
            MenuItemType.SUPPORT,
            MenuItemType.TRANSFER,
            MenuItemType.CASH_DEPOSIT,
            MenuItemType.CASH_OUT,
        ).forEach { item ->
            assertTrue(item.requiresTerminalConfiguration)
        }
    }

    @Test
    fun `sadad features show bill inquiry and hide transfer`() {
        val visible = visibleHomeMenuItems(
            enabledFeatures = setOf(
                "PURCHASE",
                "TOPUP",
                "BILL",
                "BILL_INQUIRY",
                "BALANCE",
                "SETTINGS",
                "REPORT",
                "VOUCHER",
            ),
            disabledFeatures = emptySet(),
        )

        assertTrue(visible.contains(MenuItemType.BILL_INQUIRY))
        assertTrue(visible.contains(MenuItemType.BILL))
        assertFalse(visible.contains(MenuItemType.TRANSFER))
    }

    @Test
    fun `hamrahPay features keep transfer and hide bill inquiry`() {
        val visible = visibleHomeMenuItems(
            enabledFeatures = setOf(
                "TRANSFER",
                "CASH_DEPOSIT",
                "CASH_OUT",
                "SETTINGS",
                "REPORT",
                "PURCHASE",
                "BALANCE",
                "BILL",
            ),
            disabledFeatures = emptySet(),
        )

        assertTrue(visible.contains(MenuItemType.TRANSFER))
        assertTrue(visible.contains(MenuItemType.BILL))
        assertFalse(visible.contains(MenuItemType.BILL_INQUIRY))
    }

    @Test
    fun `settings and report stay accessible without configuration`() {
        assertFalse(MenuItemType.SETTINGS.requiresTerminalConfiguration)
        assertFalse(MenuItemType.REPORT.requiresTerminalConfiguration)
    }
}
