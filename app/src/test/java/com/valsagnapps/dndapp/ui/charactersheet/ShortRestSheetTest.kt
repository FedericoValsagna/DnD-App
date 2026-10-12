package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.HitDice
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** With the default (small) screen: with the phone one a dialog never gets idle. */
@RunWith(AndroidJUnit4::class)
class ShortRestSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val tordek = sampleCharacter(name = "Tordek").copy(currentHitPoints = 20)
    private val rest = CombatDialog.ShortRest(
        hitDice = listOf(HitDice(10, 5, used = 1)),
        currentHitPoints = 20,
        maxHitPoints = 44,
        constitutionModifier = 2,
        canSpendDice = true,
    )
    private var confirmed = 0

    @Test
    fun `shows the dice left and rests without dice`() {
        show(rest)

        composeRule.onNodeWithText(context.getString(R.string.hit_dice_left, 4), substring = true).assertIsDisplayed()
        rest().assertIsEnabled().performClick()

        assertEquals(1, confirmed)
    }

    @Test
    fun `spending dice asks for the roll and previews the hit points`() {
        show(rest)

        composeRule.onNodeWithContentDescription(context.getString(R.string.hit_die_more, 10)).performClick()
        rest().assertIsNotEnabled()
        composeRule.onNodeWithText("7").performClick()

        composeRule.onNodeWithText("29/44").assertIsDisplayed()
        rest().assertIsEnabled()
    }

    @Test
    fun `at 0 hit points the dice can't be spent`() {
        show(rest.copy(currentHitPoints = 0, canSpendDice = false))

        composeRule.onNodeWithContentDescription(context.getString(R.string.hit_die_more, 10)).assertIsNotEnabled()
    }

    private fun rest() = composeRule.onNodeWithText(context.getString(R.string.rest)).performScrollTo()

    private fun show(dialog: CombatDialog.ShortRest) {
        var state by mutableStateOf(CharacterSheetUiState.Content(tordek, combat = dialog))
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
