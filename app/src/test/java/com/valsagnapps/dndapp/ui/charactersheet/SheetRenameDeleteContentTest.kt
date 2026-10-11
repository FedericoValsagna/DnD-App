package com.valsagnapps.dndapp.ui.charactersheet

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
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

/** Runs on Robolectric's default screen: on [PHONE_SCREEN] a dialog never gets idle (see docs/gotchas.md). */
@RunWith(AndroidJUnit4::class)
class SheetRenameDeleteContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tordek = sampleCharacter(id = "1", name = "Tordek")

    @Test
    fun `in edit mode the name, the abilities and the delete menu item open their dialogs`() {
        val started = mutableListOf<SheetEditTarget>()
        setContent(
            CharacterSheetUiState.Content(tordek, isEditing = true),
            SheetEditActions(onStartEdit = {
                started +=
                    it
            }),
        )

        composeRule.onNodeWithText("Tordek").performClick()
        // DEX's score, in its box on Skills.
        composeRule.onNodeWithText("12").performClick()
        composeRule.onNodeWithContentDescription(context.getString(R.string.sheet_menu)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.delete_character)).performClick()

        assertEquals(
            listOf(SheetEditTarget.NAME, SheetEditTarget.ABILITY_SCORES, SheetEditTarget.DELETE_CHARACTER),
            started,
        )
    }

    @Test
    fun `the name dialog reports the changes and the save`() {
        var uiState by mutableStateOf(CharacterSheetUiState.Content(tordek, edit = SheetEdit.Name("Tordek")))
        var confirmed = false
        setContent(
            { uiState },
            SheetEditActions(
                onNameChange = { uiState = uiState.copy(edit = SheetEdit.Name(it)) },
                onConfirm = { confirmed = true },
            ),
        )

        composeRule.onNode(textField(R.string.name)).performTextReplacement("Krusk")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()

        assertEquals(SheetEdit.Name("Krusk"), uiState.edit)
        assertEquals(true, confirmed)
    }

    @Test
    fun `a blank name shows the error and cannot be saved`() {
        setContent(CharacterSheetUiState.Content(tordek, edit = SheetEdit.Name(" ")))

        composeRule.onNodeWithText(context.getString(R.string.name_error, 100)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.save)).assertIsNotEnabled()
    }

    @Test
    fun `the abilities dialog has the six scores and reports the changes`() {
        val changes = mutableListOf<Pair<Ability, String>>()
        val scores = Ability.entries.associateWith { "10" } + (Ability.WISDOM to "0")
        setContent(
            CharacterSheetUiState.Content(tordek, edit = SheetEdit.AbilityScores(scores)),
            SheetEditActions(onAbilityScoreChange = { ability, value -> changes += ability to value }),
        )

        composeRule.onNodeWithText(context.getString(R.string.range_error, 1, 30)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.save)).assertIsNotEnabled()
        composeRule.onNode(textField(R.string.ability_wisdom)).performTextReplacement("14")

        assertEquals(listOf(Ability.WISDOM to "14"), changes)
    }

    @Test
    fun `the delete confirmation names the character and reports the confirmation`() {
        var confirmed = false
        setContent(
            CharacterSheetUiState.Content(tordek, isEditing = true, edit = SheetEdit.DeleteCharacter("Tordek")),
            SheetEditActions(onConfirm = { confirmed = true }),
        )

        composeRule.onNodeWithText(context.getString(R.string.delete_character_title, "Tordek")).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.delete_character_message)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.delete)).performClick()

        assertEquals(true, confirmed)
    }

    @Test
    fun `the delete confirmation shows the error when the delete fails`() {
        setContent(
            CharacterSheetUiState.Content(
                tordek,
                edit = SheetEdit.DeleteCharacter("Tordek"),
                editError = RepositoryError.Network,
            ),
        )

        composeRule.onNodeWithText(
            context.getString(R.string.delete_error, context.getString(R.string.error_network)),
        ).assertIsDisplayed()
    }

    private fun textField(@StringRes label: Int) = hasSetTextAction() and hasText(context.getString(label))

    private fun setContent(uiState: CharacterSheetUiState, actions: SheetEditActions = SheetEditActions()) =
        setContent({ uiState }, actions)

    private fun setContent(uiState: () -> CharacterSheetUiState, actions: SheetEditActions) {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState(),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                editActions = actions,
                initialDestination = SheetDestination.SKILLS,
            )
        }
    }
}
