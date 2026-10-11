package com.valsagnapps.dndapp.ui

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import com.valsagnapps.dndapp.ui.charactersheet.PHONE_SCREEN
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = PHONE_SCREEN)
class DnDNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = FakeCharacterRepository(listOf(sampleCharacter(id = "1", name = "Tordek")))

    @Test
    fun `opens the sheet of a listed character and goes back to the list`() {
        setContent()

        composeRule.onNodeWithText("Tordek").performClick()
        composeRule.onNodeWithText(context.getString(R.string.passive_perception).uppercase()).assertIsDisplayed()

        pressBack()
        composeRule.onNodeWithText(context.getString(R.string.character_list_title)).assertIsDisplayed()
    }

    @Test
    fun `shows the sheet after creating a character and going back returns to the list`() {
        setContent()

        composeRule.onNodeWithText(context.getString(R.string.create_character)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.name)).performTextInput("Mialee")
        composeRule.onNodeWithText(context.getString(R.string.character_class)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.class_cleric)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.max_hit_points)).performTextInput("8")
        composeRule.onNodeWithText(context.getString(R.string.create)).performScrollTo().performClick()

        // The form was replaced by the new character's sheet.
        composeRule.onNodeWithText("Mialee").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.passive_perception).uppercase()).assertIsDisplayed()

        pressBack()
        composeRule.onNodeWithText(context.getString(R.string.character_list_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Mialee").assertIsDisplayed()
    }

    @Test
    fun `deleting the character from the sheet returns to the list without it`() {
        setContent()

        composeRule.onNodeWithText("Tordek").performClick()
        startEditing()
        composeRule.onNodeWithContentDescription(context.getString(R.string.sheet_menu)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.delete_character)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.delete)).performClick()

        composeRule.onNodeWithText(context.getString(R.string.character_list_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.character_list_empty)).assertIsDisplayed()
    }

    @Test
    fun `changing a skill in the sheet shows the value the repository returns`() {
        setContent()

        composeRule.onNodeWithText("Tordek").performClick()
        startEditing()
        composeRule.onNodeWithText(context.getString(R.string.skill_stealth)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.proficiency_expertise)).performClick()

        composeRule.onNodeWithContentDescription(context.getString(R.string.proficiency_expertise)).assertIsDisplayed()
    }

    private lateinit var backDispatcher: OnBackPressedDispatcher

    private fun setContent() {
        composeRule.setContent {
            backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current).onBackPressedDispatcher
            DnDNavigation(repository)
        }
    }

    /** The system's back: the sheet has no back button of its own. */
    private fun pressBack() {
        composeRule.runOnUiThread { backDispatcher.onBackPressed() }
    }

    private fun startEditing() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.sheet_menu)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.edit_sheet)).performClick()
    }
}
