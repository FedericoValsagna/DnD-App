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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.Ability
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** What all the sheet's screens share: the header with its menu, the bottom bar and the dialogs. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = PHONE_SCREEN)
class CharacterSheetContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tordek = sampleCharacter(name = "Tordek")

    @Test
    fun `the header shows the name and the max hit points`() {
        show(CharacterSheetUiState.Content(tordek))

        composeRule.onNodeWithText("Tordek").assertIsDisplayed()
        composeRule.onNodeWithText("44").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.max_hit_points)).assertIsDisplayed()
    }

    @Test
    fun `the header shows a dash when an older server does not send the max hit points`() {
        show(CharacterSheetUiState.Content(tordek.copy(maxHitPoints = null)))

        composeRule.onNodeWithContentDescription(context.getString(R.string.max_hit_points)).assertIsDisplayed()
        // The HP, and the six saving throws that sampleCharacter doesn't have.
        composeRule.onAllNodesWithText(context.getString(R.string.missing_value))
            .assertCountEquals(1 + Ability.entries.size)
    }

    @Test
    fun `opens on Combate and the bottom bar goes to Skills, Personaje and back`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(tordek), onRetry = {
            }, onSkillProficiencyChange = { _, _ -> })
        }

        composeRule.onNodeWithText(context.getString(R.string.combat_no_actions)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.skills)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.passive_perception).uppercase()).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.character)).performClick()
        composeRule.onNodeWithText("Fighter 5").assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.passive_perception).uppercase())
            .assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.combat)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.combat_no_actions)).assertIsDisplayed()
    }

    @Test
    fun `the menu toggles edit mode`() {
        var toggles = 0
        var uiState by mutableStateOf(CharacterSheetUiState.Content(tordek))
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                editActions = SheetEditActions(
                    onToggleEditing = {
                        toggles++
                        uiState = uiState.copy(isEditing = !uiState.isEditing)
                    },
                ),
                initialDestination = SheetDestination.SKILLS,
            )
        }

        openMenu()
        composeRule.onNodeWithText(context.getString(R.string.edit_sheet)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.skills_edit_hint)).assertIsDisplayed()
        openMenu()
        composeRule.onNodeWithText(context.getString(R.string.finish_editing)).performClick()

        assertEquals(2, toggles)
        composeRule.onAllNodesWithText(context.getString(R.string.skills_edit_hint)).assertCountEquals(0)
    }

    @Test
    fun `the menu has the rests disabled and no delete outside edit mode`() {
        show(CharacterSheetUiState.Content(tordek))

        openMenu()

        composeRule.onNodeWithText(context.getString(R.string.short_rest)).assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.long_rest)).assertIsNotEnabled()
        composeRule.onAllNodesWithText(context.getString(R.string.delete_character)).assertCountEquals(0)
    }

    @Test
    fun `in read mode the name cannot be edited`() {
        var edited = false
        show(CharacterSheetUiState.Content(tordek), SheetEditActions(onStartEdit = { edited = true }))

        composeRule.onNodeWithText("Tordek").performClick()

        assertEquals(false, edited)
    }

    @Test
    fun `shows error when character is not found`() {
        show(CharacterSheetUiState.Error(RepositoryError.NotFound))

        composeRule.onNodeWithText(context.getString(R.string.error_not_found)).assertIsDisplayed()
    }

    private fun openMenu() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.sheet_menu)).performClick()
    }

    private fun show(uiState: CharacterSheetUiState, editActions: SheetEditActions = SheetEditActions()) {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                editActions = editActions,
                initialDestination = SheetDestination.SKILLS,
            )
        }
    }
}
