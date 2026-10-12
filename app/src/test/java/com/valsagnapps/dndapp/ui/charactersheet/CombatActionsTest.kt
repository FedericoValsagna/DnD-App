package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.flurryOfBlows
import com.valsagnapps.dndapp.data.sampleMonk
import com.valsagnapps.dndapp.domain.ClassFeature
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = PHONE_SCREEN)
class CombatActionsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun show(combatActions: CombatActions = CombatActions()) {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = CharacterSheetUiState.Content(sampleMonk()),
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                combatActions = combatActions,
            )
        }
    }

    @Test
    fun `shows the actions by type with their uses`() {
        show()

        listOf(R.string.action_type_action, R.string.action_type_bonus_action, R.string.action_type_reaction)
            .forEach { composeRule.onNodeWithText(context.getString(it).uppercase()).assertIsDisplayed() }
        composeRule.onNodeWithText("Flurry of Blows").assertIsDisplayed()
        composeRule.onNodeWithText("Deflect Missiles").assertIsDisplayed()
        composeRule.onNodeWithText("5/6").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.use_pip, "Wholeness of Body", 1, 1))
            .assertIsDisplayed()
    }

    @Test
    fun `tapping a full point spends a use`() {
        var change: Pair<String, Int>? = null
        show(CombatActions(onResourceUsedChange = { resource, used -> change = resource to used }))

        composeRule.onNodeWithContentDescription(context.getString(R.string.use_pip, "Wholeness of Body", 1, 1))
            .performClick()

        assertEquals("WHOLENESS_OF_BODY" to 1, change)
    }

    @Test
    fun `tapping a row opens its sheet`() {
        var opened: ClassFeature? = null
        show(CombatActions(onOpenFeature = { opened = it }))

        composeRule.onNodeWithText("Flurry of Blows").performClick()

        assertEquals(flurryOfBlows, opened)
    }
}
