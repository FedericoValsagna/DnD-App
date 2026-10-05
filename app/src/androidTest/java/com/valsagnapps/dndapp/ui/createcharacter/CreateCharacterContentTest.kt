package com.valsagnapps.dndapp.ui.createcharacter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CreateCharacterContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `sends typed values and the save click to the view model`() {
        var name = ""
        var strength = ""
        var saved = false
        composeRule.setContent {
            CreateCharacterContent(
                uiState = CreateCharacterUiState(
                    abilityScores = Ability.entries.associateWith { "10" } + (Ability.STRENGTH to ""),
                ),
                onNameChange = { name = it },
                onLevelChange = {},
                onAbilityScoreChange = { ability, value -> if (ability == Ability.STRENGTH) strength = value },
                onSave = { saved = true },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.name)).performTextInput("Tordek")
        composeRule.onNodeWithText(context.getString(R.string.ability_strength)).performTextInput("16")
        composeRule.onNodeWithText(context.getString(R.string.create)).performClick()

        assertEquals("Tordek", name)
        assertEquals("16", strength)
        assertEquals(true, saved)
    }

    @Test
    fun `shows validation errors after trying to save`() {
        composeRule.setContent {
            CreateCharacterContent(
                uiState = CreateCharacterUiState(level = "25", showValidationErrors = true),
                onNameChange = {},
                onLevelChange = {},
                onAbilityScoreChange = { _, _ -> },
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.name_error, 100)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.range_error, 1, 20)).assertIsDisplayed()
    }

    @Test
    fun `shows the save error`() {
        composeRule.setContent {
            CreateCharacterContent(
                uiState = CreateCharacterUiState(saveError = RepositoryError.Network),
                onNameChange = {},
                onLevelChange = {},
                onAbilityScoreChange = { _, _ -> },
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.error_network)).assertIsDisplayed()
    }
}
