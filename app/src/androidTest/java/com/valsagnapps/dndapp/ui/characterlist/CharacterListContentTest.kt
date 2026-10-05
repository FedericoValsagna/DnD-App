package com.valsagnapps.dndapp.ui.characterlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Character
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CharacterListContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tordek =
        Character(id = "1", name = "Tordek", level = 5, proficiencyBonus = 3, abilities = emptyMap())

    @Test
    fun `shows the characters and reports which one was clicked`() {
        var clickedId: String? = null
        composeRule.setContent {
            CharacterListContent(
                uiState = CharacterListUiState.Content(listOf(tordek)),
                onRetry = {},
                onCharacterClick = { clickedId = it },
                onCreateClick = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.level_value, 5)).assertIsDisplayed()
        composeRule.onNodeWithText("Tordek").performClick()

        assertEquals("1", clickedId)
    }

    @Test
    fun `shows the empty message when there are no characters`() {
        composeRule.setContent {
            CharacterListContent(CharacterListUiState.Content(emptyList()), {}, {}, {})
        }

        composeRule.onNodeWithText(context.getString(R.string.character_list_empty)).assertIsDisplayed()
    }

    @Test
    fun `shows the error and retries`() {
        var retried = false
        composeRule.setContent {
            CharacterListContent(
                uiState = CharacterListUiState.Error(RepositoryError.Network),
                onRetry = { retried = true },
                onCharacterClick = {},
                onCreateClick = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.retry)).performClick()

        assertEquals(true, retried)
    }

    @Test
    fun `create button reports the click`() {
        var createClicked = false
        composeRule.setContent {
            CharacterListContent(CharacterListUiState.Content(emptyList()), {}, {}, { createClicked = true })
        }

        composeRule.onNodeWithText(context.getString(R.string.create_character)).performClick()

        assertEquals(true, createClicked)
    }
}
