package org.opennur.quran

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesReaderWithBundledContent() {
        waitForReader()

        composeRule.onNodeWithText("Quran").assertIsDisplayed()
        composeRule.onNodeWithText("Read offline").assertIsDisplayed()
    }

    @Test
    fun opensSettingsFromReader() {
        waitForReader()

        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Show Indonesian translation").assertIsDisplayed()
    }

    @Test
    fun searchesAndOpensAnAyah() {
        waitForReader()

        composeRule.onNodeWithContentDescription("Search ayahs").performClick()
        composeRule.onNodeWithText("Search").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("Maha Pengasih")
        waitForText("Al-Fatihah", substring = true)

        composeRule.onNodeWithText("Al-Fatihah  ·  1").performClick()
        composeRule.onNodeWithText("Quran").assertIsDisplayed()
        composeRule.onNodeWithText("Al-Fatihah").assertIsDisplayed()
    }

    private fun waitForText(text: String, substring: Boolean = false) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(text, substring = substring)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        assertTrue("Expected text '$text' to appear", composeRule.onAllNodesWithText(text, substring = substring)
            .fetchSemanticsNodes()
            .isNotEmpty())
    }

    private fun waitForReader() {
        waitForText("Read offline")
    }
}
