package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
        skills = mapOf(Skill.STEALTH to SkillValue(Proficiency.EXPERTISE, 7)),
        passivePerception = 11,
    )

    @Test
    fun `shows the character values from the server`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(tordek), {}, { _, _ -> }, {})
        }

        composeRule.onNodeWithText("Tordek").assertIsDisplayed()
        composeRule.onNodeWithText("11").assertIsDisplayed()
        // Proficiency bonus and STR modifier.
        composeRule.onAllNodesWithText("+3").assertCountEquals(2)
        composeRule.onNodeWithText(context.getString(R.string.ability_score_value, 16))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("-1").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `shows the classes, max hit points and hit dice`() {
        val multiclass = tordek.copy(
            classes = listOf(ClassLevel(CharacterClass.FIGHTER, 3), ClassLevel(CharacterClass.CLERIC, 2)),
            maxHitPoints = 47,
            hitDice = listOf(HitDice(die = 10, count = 3), HitDice(die = 8, count = 2)),
        )
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(multiclass), {}, { _, _ -> }, {})
        }

        composeRule.onNodeWithText("Fighter 3 / Cleric 2").assertIsDisplayed()
        composeRule.onNodeWithText("47").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("3d10 + 2d8").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `shows the saving throws with their bonus and proficiency`() {
        val withSaves = tordek.copy(
            savingThrows = mapOf(
                Ability.WISDOM to SavingThrow(Proficiency.PROFICIENT, 9),
                Ability.DEXTERITY to SavingThrow(Proficiency.NONE, -2),
            ),
        )
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(withSaves), {}, { _, _ -> }, {})
        }

        composeRule.onNodeWithText(context.getString(R.string.saving_throws)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("+9").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("-2").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.proficiency_proficient))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `shows a dash for the values an older server does not send`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(tordek), {}, { _, _ -> }, {})
        }

        // Class summary, max HP, hit dice and the six saving throws, plus the abilities and skills tordek lacks.
        val missingAbilities = Ability.entries.size - tordek.abilities.size
        val missingSkills = Skill.entries.size - tordek.skills.size
        composeRule.onAllNodesWithText(context.getString(R.string.missing_value))
            .assertCountEquals(3 + Ability.entries.size + missingAbilities + missingSkills)
    }

    @Test
    fun `shows the skills with their bonus and proficiency`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(tordek), {}, { _, _ -> }, {})
        }

        composeRule.onNodeWithText(context.getString(R.string.skill_stealth)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("+7").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.proficiency_expertise))
            .performScrollTo()
            .assertIsDisplayed()
        // Skills the server didn't send show no value.
        composeRule.onNodeWithText(context.getString(R.string.skill_arcana)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `picking a proficiency in the menu reports the change`() {
        var change: Pair<Skill, Proficiency>? = null
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(tordek),
                onRetry = {},
                onSkillProficiencyChange = { skill, proficiency -> change = skill to proficiency },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.skill_arcana)).performScrollTo().performClick()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_proficient)).performClick()

        assertEquals(Skill.ARCANA to Proficiency.PROFICIENT, change)
    }

    @Test
    fun `shows the error when the skills could not be saved`() {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(tordek, skillsError = RepositoryError.Network),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
            )
        }

        val message = context.getString(R.string.skills_save_error, context.getString(R.string.error_network))
        composeRule.onNodeWithText(message).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `skills cannot be changed while saving`() {
        var changed = false
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(tordek, isSavingSkills = true),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> changed = true },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.skill_arcana)).performScrollTo().performClick()

        composeRule.onAllNodesWithText(context.getString(R.string.proficiency_proficient)).assertCountEquals(0)
        assertEquals(false, changed)
    }

    @Test
    fun `shows error when character is not found`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Error(RepositoryError.NotFound), {}, { _, _ -> }, {})
        }

        composeRule.onNodeWithText(context.getString(R.string.error_not_found)).assertIsDisplayed()
    }

    @Test
    fun `back button reports the click`() {
        var backClicked = false
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Loading, {}, { _, _ -> }, { backClicked = true })
        }

        composeRule.onNodeWithContentDescription(context.getString(R.string.back)).performClick()

        assertEquals(true, backClicked)
    }

    @Test
    fun `tapping the class and the max hit points opens their editors`() {
        var editedClass = false
        var editedHitPoints = false
        val fighter = tordek.copy(classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5)), maxHitPoints = 44)
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(fighter),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
                editActions = SheetEditActions(
                    onEditClass = { editedClass = true },
                    onEditMaxHitPoints = { editedHitPoints = true },
                ),
            )
        }

        composeRule.onNodeWithText("Fighter 5").performClick()
        composeRule.onNodeWithText("44").performScrollTo().performClick()

        assertEquals(true, editedClass)
        assertEquals(true, editedHitPoints)
    }

    @Test
    fun `the class dialog reports the changes and the save`() {
        var uiState by mutableStateOf(
            CharacterSheetUiState.Content(tordek, edit = SheetEdit.Class(CharacterClass.FIGHTER, "5")),
        )
        var confirmed = false
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
                editActions = SheetEditActions(
                    onClassChange = { uiState = uiState.copy(edit = SheetEdit.Class(it, "5")) },
                    onConfirm = { confirmed = true },
                ),
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.class_fighter)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.class_cleric)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()

        assertEquals(SheetEdit.Class(CharacterClass.CLERIC, "5"), uiState.edit)
        assertEquals(true, confirmed)
    }

    @Test
    fun `the edit dialog shows the save error and cannot save an invalid value`() {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(
                    tordek,
                    edit = SheetEdit.MaxHitPoints("0"),
                    editError = RepositoryError.Network,
                ),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(
            context.getString(R.string.edit_save_error, context.getString(R.string.error_network)),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.range_error, 1, 999)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.save)).assertIsNotEnabled()
    }
}
