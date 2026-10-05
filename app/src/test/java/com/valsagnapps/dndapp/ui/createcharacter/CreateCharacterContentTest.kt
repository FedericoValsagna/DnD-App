package com.valsagnapps.dndapp.ui.createcharacter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateCharacterContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `sends typed values and the save click to the view model`() {
        // The typed values flow back into the state, like the ViewModel does in the app.
        var uiState by mutableStateOf(
            CreateCharacterUiState(abilityScores = Ability.entries.associateWith { "10" } + (Ability.STRENGTH to "")),
        )
        var saved = false
        composeRule.setContent {
            CreateCharacterContent(
                uiState = uiState,
                onNameChange = { uiState = uiState.copy(name = it) },
                onLevelChange = {},
                onAbilityScoreChange = { ability, value ->
                    uiState = uiState.copy(abilityScores = uiState.abilityScores + (ability to value))
                },
                onSkillProficiencyChange = { skill, proficiency ->
                    uiState = uiState.copy(skills = uiState.skills + (skill to proficiency))
                },
                onSave = { saved = true },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.name)).performTextInput("Tordek")
        composeRule.onNodeWithText(
            context.getString(R.string.ability_strength),
        ).performScrollTo().performTextInput("16")
        composeRule.onNodeWithText(context.getString(R.string.skill_stealth)).performScrollTo().performClick()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_expertise)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.create)).performScrollTo().performClick()

        assertEquals("Tordek", uiState.name)
        assertEquals("16", uiState.abilityScores[Ability.STRENGTH])
        assertEquals(mapOf(Skill.STEALTH to Proficiency.EXPERTISE), uiState.skills)
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
                onSkillProficiencyChange = { _, _ -> },
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
                onSkillProficiencyChange = { _, _ -> },
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.error_network)).performScrollTo().assertIsDisplayed()
    }
}
