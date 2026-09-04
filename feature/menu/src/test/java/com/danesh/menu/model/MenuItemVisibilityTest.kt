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
    fun `settings and report stay accessible without configuration`() {
        assertFalse(MenuItemType.SETTINGS.requiresTerminalConfiguration)
        assertFalse(MenuItemType.REPORT.requiresTerminalConfiguration)
    }
}
