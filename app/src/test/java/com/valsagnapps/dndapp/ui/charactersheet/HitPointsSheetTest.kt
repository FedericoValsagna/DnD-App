package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.LifeStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The combat dialogs, with the default (small) screen: with the phone one a dialog never gets idle. */
@RunWith(AndroidJUnit4::class)
class HitPointsSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val aldric = sampleCharacter(name = "Aldric").copy(
        maxHitPoints = 44,
        currentHitPoints = 12,
        temporaryHitPoints = 5,
    )
    private var confirmed = 0

    @Test
    fun `typing an amount shows how the hit points are left`() {
        show(aldric, CombatDialog.HitPoints())

        composeRule.onNodeWithText(context.getString(R.string.apply_amount_missing)).assertIsNotEnabled()
        composeRule.onNodeWithText("8").performClick()

        composeRule.onNodeWithText("12/44 +5 → 9/44").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.apply_damage, 8)).performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun `changing the action changes the preview and the button`() {
        show(aldric, CombatDialog.HitPoints(amount = "8"))

        composeRule.onNodeWithText(context.getString(R.string.healing)).performClick()
        composeRule.onNodeWithText("12/44 +5 → 20/44 +5").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.apply_healing, 8)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.temporary_hit_points)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.apply_temporary, 8)).assertIsDisplayed()
    }

    @Test
    fun `backspace and clear erase the amount`() {
        show(aldric, CombatDialog.HitPoints(amount = "128"))

        composeRule.onNodeWithContentDescription(context.getString(R.string.keypad_backspace)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.apply_damage, 12)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.keypad_clear)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.apply_amount_missing)).assertIsDisplayed()
    }

    @Test
    fun `critical only shows for damage at 0 hit points`() {
        show(aldric, CombatDialog.HitPoints(amount = "6"))

        composeRule.onAllNodesWithText(context.getString(R.string.critical)).assertCountEquals(0)
    }

    @Test
    fun `a critical hit at 0 hit points adds two failures`() {
        val dying = aldric.copy(currentHitPoints = 0, temporaryHitPoints = 0, lifeStatus = LifeStatus.DYING)
        show(dying, CombatDialog.HitPoints(amount = "6"))

        composeRule.onNodeWithText("0/44 → +1 fallo").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.critical)).performClick()
        composeRule.onNodeWithText("0/44 → +2 fallos").assertIsDisplayed()
    }

    @Test
    fun `shows the error and keeps the amount`() {
        show(aldric, CombatDialog.HitPoints(amount = "8"), error = RepositoryError.Network)

        composeRule.onNodeWithText(
            context.getString(R.string.edit_save_error, context.getString(R.string.error_network)),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.apply_damage, 8)).assertIsDisplayed()
    }

    @Test
    fun `reviving asks for the hit points`() {
        val dead = aldric.copy(currentHitPoints = 0, temporaryHitPoints = 0, lifeStatus = LifeStatus.DEAD)
        show(dead, CombatDialog.Revive(maxHitPoints = 44))

        composeRule.onNodeWithText(context.getString(R.string.revive_hit_points_hint, 44)).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextReplacement("45")
        reviveButton().assertIsNotEnabled()
        composeRule.onNode(hasSetTextAction()).performTextReplacement("44")
        reviveButton().performClick()

        assertEquals(1, confirmed)
    }

    /** The dialog's button, not the one in the dead box behind it. */
    private fun reviveButton() = composeRule.onNode(
        hasText(context.getString(R.string.revive)) and hasClickAction() and hasAnyAncestor(isDialog()),
    )

    /** The dialog changes as the ViewModel would change it. */
    private fun show(character: Character, dialog: CombatDialog, error: RepositoryError? = null) {
        var state by mutableStateOf(CharacterSheetUiState.Content(character, combat = dialog, combatError = error))
        composeRule.setContent {
            CharacterSheetContent(
                uiState = state,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                combatActions = CombatActions(
                    onInput = { input -> state = state.copy(combat = state.combat?.changedBy(input)) },
                    onConfirm = { confirmed++ },
                ),
            )
        }
    }
}
