package com.danesh.balance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BalanceSuccessBadgeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun balanceSuccessBadge_isDisplayed() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF01242F)),
            ) {
                BalanceSuccessBadge()
            }
        }

        composeRule
            .onNodeWithTag(BALANCE_SUCCESS_BADGE_TEST_TAG)
            .assertIsDisplayed()
    }
}
