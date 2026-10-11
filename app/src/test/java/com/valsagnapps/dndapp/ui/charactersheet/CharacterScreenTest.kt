package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.HitDice
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = PHONE_SCREEN)
class CharacterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val tordek = sampleCharacter(name = "Tordek").copy(
        maxHitPoints = 47,
        classes = listOf(ClassLevel(CharacterClass.FIGHTER, 3), ClassLevel(CharacterClass.CLERIC, 2)),
        hitDice = listOf(HitDice(die = 10, count = 3), HitDice(die = 8, count = 2)),
    )

    @Test
    fun `shows the classes, proficiency bonus, max hit points, hit dice and speed`() {
        show(CharacterSheetUiState.Content(tordek.copy(speed = 40, baseSpeed = 30)))

        composeRule.onNodeWithText("Fighter 3 / Cleric 2").assertIsDisplayed()
        composeRule.onNodeWithText("+3").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.speed)).assertIsDisplayed()
        composeRule.onNodeWithText("40").assertIsDisplayed()
        // In the class block and in the character header.
        composeRule.onAllNodesWithText("47").assertCountEquals(2)
        composeRule.onNodeWithText("3d10 + 2d8").assertIsDisplayed()
    }

    @Test
    fun `shows a dash for the values an older server does not send`() {
        show(
            CharacterSheetUiState.Content(
                tordek.copy(classes = emptyList(), maxHitPoints = null, hitDice = emptyList()),
            ),
        )

        // Class summary, max HP (also in the character header), hit dice and speed; then armor, weapons and tools.
        composeRule.onAllNodesWithText(context.getString(R.string.missing_value)).assertCountEquals(5 + 3)
    }

    @Test
    fun `the tabs switch between proficiencies and features`() {
        show(CharacterSheetUiState.Content(tordek))

        composeRule.onNodeWithText(context.getString(R.string.armor)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.features)).performClick()

        composeRule.onAllNodesWithText(context.getString(R.string.armor)).assertCountEquals(0)
    }

    @Test
    fun `in edit mode tapping the class, the max hit points and the speed opens their editors`() {
        val started = mutableListOf<SheetEditTarget>()
        val fighter = tordek.copy(
            classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5)),
            maxHitPoints = 44,
            speed = 35,
            baseSpeed = 35,
        )
        show(
            CharacterSheetUiState.Content(fighter, isEditing = true),
            SheetEditActions(onStartEdit = { started += it }),
        )

        composeRule.onNodeWithText(context.getString(R.string.character_edit_hint)).assertIsDisplayed()
        composeRule.onNodeWithText("Fighter 5").performClick()
        maxHitPointsCell().performClick()
        composeRule.onNode(hasClickAction() and hasText("35")).performClick()

        assertEquals(
            listOf(SheetEditTarget.CLASS, SheetEditTarget.MAX_HIT_POINTS, SheetEditTarget.SPEED),
            started,
        )
    }

    @Test
    fun `in read mode the class and the max hit points cannot be edited`() {
        var edited = false
        val fighter = tordek.copy(classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5)), maxHitPoints = 44)
        show(CharacterSheetUiState.Content(fighter), SheetEditActions(onStartEdit = { edited = true }))

        composeRule.onNodeWithText("Fighter 5").performClick()
        composeRule.onAllNodes(hasClickAction() and hasText("44")).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.character_edit_hint)).assertCountEquals(0)
        assertEquals(false, edited)
    }

    @Test
    fun `a multiclass character cannot edit the class`() {
        var edited = false
        show(CharacterSheetUiState.Content(tordek, isEditing = true), SheetEditActions(onStartEdit = { edited = true }))

        composeRule.onNodeWithText("Fighter 3 / Cleric 2").performClick()

        assertEquals(false, edited)
    }

    /** The Max HP cell of the class block (its label and value merge in a clickable node), not the header's HP. */
    private fun maxHitPointsCell() = composeRule.onNode(hasClickAction() and hasText("44"))

    private fun show(uiState: CharacterSheetUiState, editActions: SheetEditActions = SheetEditActions()) {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                editActions = editActions,
                initialDestination = SheetDestination.CHARACTER,
            )
        }
    }
}
