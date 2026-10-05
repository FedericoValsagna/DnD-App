package com.valsagnapps.dndapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.sampleCharacter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DnDNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = FakeCharacterRepository(listOf(sampleCharacter(id = "1", name = "Tordek")))

    @Test
    fun `opens the sheet of a listed character and goes back to the list`() {
        composeRule.setContent { DnDNavigation(repository) }

        composeRule.onNodeWithText("Tordek").performClick()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_bonus)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(context.getString(R.string.back)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.character_list_title)).assertIsDisplayed()
    }

    @Test
    fun `shows the sheet after creating a character and going back returns to the list`() {
        composeRule.setContent { DnDNavigation(repository) }

        composeRule.onNodeWithText(context.getString(R.string.create_character)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.name)).performTextInput("Mialee")
        composeRule.onNodeWithText(context.getString(R.string.character_class)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.class_cleric)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.max_hit_points)).performTextInput("8")
        composeRule.onNodeWithText(context.getString(R.string.create)).performScrollTo().performClick()

        // The form was replaced by the new character's sheet.
        composeRule.onNodeWithText("Mialee").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_bonus)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(context.getString(R.string.back)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.character_list_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Mialee").assertIsDisplayed()
    }

    @Test
    fun `changing a skill in the sheet shows the value the repository returns`() {
        composeRule.setContent { DnDNavigation(repository) }

        composeRule.onNodeWithText("Tordek").performClick()
        composeRule.onNodeWithText(context.getString(R.string.skill_stealth)).performScrollTo().performClick()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_expertise)).performClick()

        composeRule.onNodeWithContentDescription(context.getString(R.string.proficiency_expertise))
            .performScrollTo()
            .assertIsDisplayed()
    }
}
