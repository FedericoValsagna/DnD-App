package com.valsagnapps.dndapp.ui.charactersheet

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillChoice
import com.valsagnapps.dndapp.domain.SkillValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Skills has no scroll: it runs on a phone-sized screen, bigger than Robolectric's default. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = PHONE_SCREEN)
class SkillsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val lidda = Character(
        id = "1",
        name = "Lidda",
        level = 3,
        proficiencyBonus = 2,
        abilities = mapOf(
            Ability.STRENGTH to AbilityScore(16, 3),
            Ability.CHARISMA to AbilityScore(8, -1),
        ),
        skills = mapOf(
            Skill.STEALTH to SkillValue(Proficiency.EXPERTISE, 7),
            Skill.ATHLETICS to SkillValue(Proficiency.PROFICIENT, 5),
        ),
        passivePerception = 11,
        classes = listOf(ClassLevel(CharacterClass.ROGUE, 2), ClassLevel(CharacterClass.BARD, 1)),
        savingThrows = mapOf(
            Ability.WISDOM to SavingThrow(Proficiency.PROFICIENT, 9),
            Ability.DEXTERITY to SavingThrow(Proficiency.NONE, -2),
        ),
        skillChoices = mapOf(
            CharacterClass.ROGUE to SkillChoice(4, setOf(Skill.STEALTH, Skill.ACROBATICS, Skill.DECEPTION)),
            CharacterClass.BARD to SkillChoice(1, Skill.entries.toSet()),
        ),
    )

    @Test
    fun `shows each ability with its score and modifier`() {
        show(CharacterSheetUiState.Content(lidda))

        composeRule.onNodeWithText("16").assertIsDisplayed()
        composeRule.onNodeWithText("+3").assertIsDisplayed()
        composeRule.onNodeWithText("8").assertIsDisplayed()
        composeRule.onNodeWithText("-1").assertIsDisplayed()
        // CON has no skills, but its box is there.
        composeRule.onAllNodesWithText(context.getString(R.string.ability_constitution_short)).assertCountEquals(2)
    }

    @Test
    fun `shows the saving throws with their bonus and proficiency`() {
        show(CharacterSheetUiState.Content(lidda))

        composeRule.onNodeWithText(context.getString(R.string.saving_throws).uppercase()).assertIsDisplayed()
        composeRule.onNodeWithText("+9").assertIsDisplayed()
        composeRule.onNodeWithText("-2").assertIsDisplayed()
        composeRule.onAllNodes(hasStateDescription(R.string.proficiency_proficient)).assertCountEquals(1)
    }

    @Test
    fun `shows the skills with their bonus and proficiency, and Passive Perception`() {
        show(CharacterSheetUiState.Content(lidda))

        composeRule.onNodeWithText(context.getString(R.string.skill_stealth)).assertIsDisplayed()
        composeRule.onNodeWithText("+7").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.proficiency_expertise)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.passive_perception).uppercase()).assertIsDisplayed()
        composeRule.onNodeWithText("11").assertIsDisplayed()
    }

    @Test
    fun `shows a dash for the values an older server does not send`() {
        show(CharacterSheetUiState.Content(lidda.copy(maxHitPoints = 30)))

        val missingAbilities = Ability.entries.size - lidda.abilities.size
        val missingSaves = Ability.entries.size - lidda.savingThrows.size
        val missingSkills = Skill.entries.size - lidda.skills.size
        // Each missing ability shows a dash for its score and another for its modifier.
        composeRule.onAllNodesWithText(context.getString(R.string.missing_value))
            .assertCountEquals(2 * missingAbilities + missingSaves + missingSkills)
    }

    @Test
    fun `in edit mode the hint takes the place of Passive Perception with the skills to choose`() {
        show(CharacterSheetUiState.Content(lidda, isEditing = true))

        composeRule.onNodeWithText(context.getString(R.string.skills_edit_hint)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.skill_choice_from_list, "Rogue", 4, 1))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.skill_choice_any, "Bard", 1)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.passive_perception).uppercase())
            .assertCountEquals(0)
    }

    @Test
    fun `in edit mode marks the skills from the class lists`() {
        show(CharacterSheetUiState.Content(lidda, isEditing = true))

        composeRule.onAllNodesWithContentDescription(context.getString(R.string.class_skill_description))
            .assertCountEquals(3)
    }

    @Test
    fun `marks no skills when the server does not send skill choices`() {
        show(CharacterSheetUiState.Content(lidda.copy(skillChoices = emptyMap()), isEditing = true))

        composeRule.onAllNodesWithContentDescription(context.getString(R.string.class_skill_description))
            .assertCountEquals(0)
    }

    @Test
    fun `picking a proficiency in the menu reports the change`() {
        var change: Pair<Skill, Proficiency>? = null
        show(
            CharacterSheetUiState.Content(lidda, isEditing = true),
            onSkillProficiencyChange = { skill, proficiency -> change = skill to proficiency },
        )

        composeRule.onNodeWithText(context.getString(R.string.skill_arcana)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_proficient)).performClick()

        assertEquals(Skill.ARCANA to Proficiency.PROFICIENT, change)
    }

    @Test
    fun `in edit mode tapping an ability opens the scores dialog`() {
        val started = mutableListOf<SheetEditTarget>()
        show(
            CharacterSheetUiState.Content(lidda, isEditing = true),
            editActions = SheetEditActions(onStartEdit = { started += it }),
        )

        composeRule.onNodeWithText("16").performClick()

        assertEquals(listOf(SheetEditTarget.ABILITY_SCORES), started)
    }

    @Test
    fun `in read mode nothing can be changed and the class suggestions are hidden`() {
        var edited = false
        show(
            CharacterSheetUiState.Content(lidda),
            onSkillProficiencyChange = { _, _ -> edited = true },
            editActions = SheetEditActions(onStartEdit = { edited = true }),
        )

        composeRule.onNodeWithText("16").performClick()
        composeRule.onNodeWithText(context.getString(R.string.skill_arcana)).performClick()

        composeRule.onAllNodesWithText(context.getString(R.string.proficiency_proficient)).assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(context.getString(R.string.class_skill_description))
            .assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.skills_edit_hint)).assertCountEquals(0)
        assertEquals(false, edited)
    }

    @Test
    fun `skills cannot be changed while saving`() {
        var changed = false
        show(
            CharacterSheetUiState.Content(lidda, isEditing = true, isSavingSkills = true),
            onSkillProficiencyChange = { _, _ -> changed = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.skill_arcana)).performClick()

        composeRule.onAllNodesWithText(context.getString(R.string.proficiency_proficient)).assertCountEquals(0)
        assertEquals(false, changed)
    }

    @Test
    fun `shows the error when the skills could not be saved`() {
        show(CharacterSheetUiState.Content(lidda, isEditing = true, skillsError = RepositoryError.Network))

        val message = context.getString(R.string.skills_save_error, context.getString(R.string.error_network))
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    private fun hasStateDescription(@StringRes proficiency: Int) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, context.getString(proficiency))

    private fun show(
        uiState: CharacterSheetUiState,
        onSkillProficiencyChange: (Skill, Proficiency) -> Unit = { _, _ -> },
        editActions: SheetEditActions = SheetEditActions(),
    ) {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = onSkillProficiencyChange,
                editActions = editActions,
            )
        }
    }
}
