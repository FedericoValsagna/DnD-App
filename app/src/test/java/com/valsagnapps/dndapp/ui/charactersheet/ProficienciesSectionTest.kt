package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.ArmorProficiency
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.Proficiencies
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillChoice
import com.valsagnapps.dndapp.domain.SkillValue
import com.valsagnapps.dndapp.domain.ToolCategory
import com.valsagnapps.dndapp.domain.ToolChoice
import com.valsagnapps.dndapp.domain.ToolProficiency
import com.valsagnapps.dndapp.domain.WeaponProficiency
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProficienciesSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val lidda = Character(
        id = "1",
        name = "Lidda",
        level = 3,
        proficiencyBonus = 2,
        abilities = emptyMap(),
        skills = mapOf(
            Skill.STEALTH to SkillValue(Proficiency.EXPERTISE, 7),
            Skill.ATHLETICS to SkillValue(Proficiency.PROFICIENT, 2),
        ),
        classes = listOf(ClassLevel(CharacterClass.ROGUE, 2), ClassLevel(CharacterClass.BARD, 1)),
        skillChoices = mapOf(
            CharacterClass.ROGUE to SkillChoice(4, setOf(Skill.STEALTH, Skill.ACROBATICS, Skill.DECEPTION)),
            CharacterClass.BARD to SkillChoice(1, Skill.entries.toSet()),
        ),
        proficiencies = Proficiencies(
            armor = listOf(ArmorProficiency.LIGHT),
            weapons = listOf(WeaponProficiency.SIMPLE, WeaponProficiency.RAPIER),
            tools = listOf(ToolProficiency.THIEVES_TOOLS),
            toolChoices = listOf(ToolChoice(1, listOf(ToolCategory.MUSICAL_INSTRUMENT))),
        ),
    )

    private fun show(character: Character) {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(character), {}, { _, _ -> }, {})
        }
    }

    @Test
    fun `shows armor, weapons and tools`() {
        show(lidda)

        composeRule.onNodeWithText("Light armor").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Simple weapons, Rapiers").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Thieves' tools, 1 × Musical instrument (a elegir)")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `shows how many skills each class offers and how many are marked`() {
        show(lidda)

        composeRule.onNodeWithText(context.getString(R.string.skill_choice_from_list, "Rogue", 4, 1))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.skill_choice_any, "Bard", 1))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `marks the skills from the class lists`() {
        show(lidda)

        composeRule.onAllNodesWithContentDescription(context.getString(R.string.class_skill_description))
            .assertCountEquals(3)
    }

    @Test
    fun `marks no skills when the server does not send skill choices`() {
        show(lidda.copy(skillChoices = emptyMap()))

        composeRule.onAllNodesWithContentDescription(context.getString(R.string.class_skill_description))
            .assertCountEquals(0)
    }
}
