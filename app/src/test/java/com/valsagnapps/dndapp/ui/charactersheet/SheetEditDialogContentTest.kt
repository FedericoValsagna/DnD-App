package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.CharacterClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The class and max HP dialogs. They run on Robolectric's default screen: on [PHONE_SCREEN] a dialog never
 * gets idle (see docs/gotchas.md).
 */
@RunWith(AndroidJUnit4::class)
class SheetEditDialogContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tordek = sampleCharacter(name = "Tordek")

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
            )
        }

        composeRule.onNodeWithText(
            context.getString(R.string.edit_save_error, context.getString(R.string.error_network)),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.range_error, 1, 999)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.save)).assertIsNotEnabled()
    }
}
