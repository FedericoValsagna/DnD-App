package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.flurryOfBlows
import com.valsagnapps.dndapp.data.kiPool
import com.valsagnapps.dndapp.data.sampleMonk
import com.valsagnapps.dndapp.data.wholeness
import com.valsagnapps.dndapp.data.wholenessOfBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The action's sheet, with the default (small) screen: with the phone one a dialog never gets idle. */
@RunWith(AndroidJUnit4::class)
class FeatureSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var confirmed = 0
    private var change: Pair<String, Int>? = null

    private fun inSheet(text: String) = composeRule.onNode(hasText(text) and hasAnyAncestor(isDialog()))

    @Test
    fun `shows the text, the type, the resource and its recharge`() {
        show(CombatDialog.Feature(flurryOfBlows, kiPool))

        inSheet("Two unarmed strikes.").assertIsDisplayed()
        inSheet("Bonus Action · Ki · short rest").assertIsDisplayed()
    }

    @Test
    fun `spending from a pool shows how it's left`() {
        show(CombatDialog.Feature(flurryOfBlows, kiPool))

        inSheet(context.getString(R.string.apply_amount_missing)).performScrollTo().assertIsNotEnabled()
        inSheet("2").performClick()

        inSheet("5/6 → 3/6").performScrollTo().assertIsDisplayed()
        inSheet(context.getString(R.string.apply_spend, 2)).performScrollTo().performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun `recovering to a pool`() {
        show(CombatDialog.Feature(flurryOfBlows, kiPool))

        inSheet(context.getString(R.string.recover)).performClick()
        inSheet("1").performClick()

        inSheet("5/6 → 6/6").performScrollTo().assertIsDisplayed()
        inSheet(context.getString(R.string.apply_recover, 1)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `points are tapped in the sheet as in the row`() {
        show(CombatDialog.Feature(wholenessOfBody, wholeness))

        composeRule.onNode(
            hasAnyAncestor(isDialog()) and
                hasContentDescription(
                    context.getString(R.string.use_pip, "Wholeness of Body", 1, 1),
                ),
        ).performClick()

        assertEquals("WHOLENESS_OF_BODY" to 1, change)
    }

    @Test
    fun `shows the error of the last try`() {
        show(CombatDialog.Feature(flurryOfBlows, kiPool), RepositoryError.Network)

        inSheet(context.getString(R.string.edit_save_error, context.getString(R.string.error_network)))
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun show(dialog: CombatDialog, error: RepositoryError? = null) {
        var state by mutableStateOf(CharacterSheetUiState.Content(sampleMonk(), combat = dialog, combatError = error))
        composeRule.setContent {
            CharacterSheetContent(
                uiState = state,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                combatActions = CombatActions(
                    onInput = { input -> state = state.copy(combat = state.combat?.changedBy(input)) },
                    onConfirm = { confirmed++ },
                    onResourceUsedChange = { resource, used -> change = resource to used },
                ),
            )
        }
    }
}
