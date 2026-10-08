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
import com.valsagnapps.dndapp.data.battleMaster
import com.valsagnapps.dndapp.data.champion
import com.valsagnapps.dndapp.data.sampleCatalog
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Subclass in the sheet header and in the class dialog. */
@RunWith(AndroidJUnit4::class)
class SubclassContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val championTordek = sampleCharacter().copy(
        classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5, champion)),
    )

    @Test
    fun `shows the subclass next to the class`() {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(championTordek), {}, { _, _ -> }, {})
        }

        composeRule.onNodeWithText("Fighter 5 (Champion)").assertIsDisplayed()
    }

    @Test
    fun `the class dialog reports the chosen subclass`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", champion, sampleCatalog())
        var uiState by mutableStateOf(CharacterSheetUiState.Content(championTordek, edit = edit))
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
                editActions = SheetEditActions(
                    onSubclassChange = { uiState = uiState.copy(edit = edit.copy(subclass = it)) },
                ),
            )
        }

        composeRule.onNodeWithText("Champion").performClick()
        composeRule.onNodeWithText("Battle Master").performClick()

        assertEquals(battleMaster, (uiState.edit as SheetEdit.Class).subclass)
    }

    @Test
    fun `the class dialog can remove the subclass`() {
        var chosen: Any? = champion
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(
                    championTordek,
                    edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", champion, sampleCatalog()),
                ),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
                editActions = SheetEditActions(onSubclassChange = { chosen = it }),
            )
        }

        composeRule.onNodeWithText("Champion").performClick()
        composeRule.onNodeWithText(context.getString(R.string.no_subclass)).performClick()

        assertEquals(null, chosen)
    }

    @Test
    fun `below the subclass level the subclass cannot be chosen`() {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(
                    championTordek,
                    edit = SheetEdit.Class(CharacterClass.FIGHTER, "2", champion, sampleCatalog()),
                ),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.subclass_level_hint, 3)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.subclass)).assertIsNotEnabled()
    }

    @Test
    fun `without the catalog the class dialog has no subclass field`() {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(
                    championTordek,
                    edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", champion),
                ),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                onBack = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.subclass)).assertDoesNotExist()
    }
}
