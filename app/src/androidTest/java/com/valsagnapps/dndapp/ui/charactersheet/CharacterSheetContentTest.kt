package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CharacterSheetContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val tordek = Character(
        id = "1",
        name = "Tordek",
        level = 5,
        proficiencyBonus = 3,
        abilities = mapOf(
            Ability.STRENGTH to AbilityScore(16, 3),
            Ability.CHARISMA to AbilityScore(8, -1),
        ),
    )

    @Test
    fun `shows the character values from the server`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(tordek), {}, {})
        }

        composeRule.onNodeWithText("Tordek").assertIsDisplayed()
        // Proficiency bonus and STR modifier.
        composeRule.onAllNodesWithText("+3").assertCountEquals(2)
        composeRule.onNodeWithText(context.getString(R.string.ability_score_value, 16)).assertIsDisplayed()
        composeRule.onNodeWithText("-1").assertIsDisplayed()
    }

    @Test
    fun `shows error when character is not found`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Error(RepositoryError.NotFound), {}, {})
        }

        composeRule.onNodeWithText(context.getString(R.string.error_not_found)).assertIsDisplayed()
    }

    @Test
    fun `back button reports the click`() {
        var backClicked = false
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Loading, {}, { backClicked = true })
        }

        composeRule.onNodeWithContentDescription(context.getString(R.string.back)).performClick()

        assertEquals(true, backClicked)
    }
}
