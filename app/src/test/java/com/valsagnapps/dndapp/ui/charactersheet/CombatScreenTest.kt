package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
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
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.LifeStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = PHONE_SCREEN)
class CombatScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val aldric = sampleCharacter(name = "Aldric").copy(
        maxHitPoints = 44,
        currentHitPoints = 12,
        temporaryHitPoints = 5,
        armorClass = 18,
        initiative = -1,
    )
    private val dying = aldric.copy(
        currentHitPoints = 0,
        temporaryHitPoints = 0,
        lifeStatus = LifeStatus.DYING,
        deathSaves = DeathSaves(successes = 1, failures = 2),
    )

    private fun pip(kind: Int, index: Int, marked: Boolean) = composeRule.onNodeWithContentDescription(
        context.getString(
            if (marked) R.string.death_save_pip_marked else R.string.death_save_pip,
            context.getString(kind),
            index,
        ),
    )

    @Test
    fun `shows the armor class, the initiative and the hit points with the temporary ones`() {
        show(CharacterSheetUiState.Content(aldric))

        composeRule.onNodeWithText("18").assertIsDisplayed()
        composeRule.onNodeWithText("-1").assertIsDisplayed()
        composeRule.onNodeWithText("12/44").assertIsDisplayed()
        composeRule.onNodeWithText("+5").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.combat_actions_coming)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.death_saves).uppercase()).assertCountEquals(0)
    }

    @Test
    fun `tapping the hit points opens the HP sheet`() {
        var opened = false
        show(CharacterSheetUiState.Content(aldric), combatActions = CombatActions(onOpenHitPoints = { opened = true }))

        composeRule.onNodeWithText("12/44").performClick()

        assertEquals(true, opened)
    }

    @Test
    fun `at 0 hit points the death saves take the place of the actions`() {
        show(CharacterSheetUiState.Content(dying))

        composeRule.onNodeWithText(context.getString(R.string.death_saves).uppercase()).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.combat_actions_coming)).assertCountEquals(0)
        pip(R.string.death_save_successes, 1, marked = true).assertIsDisplayed()
        pip(R.string.death_save_failures, 2, marked = true).assertIsDisplayed()
        pip(R.string.death_save_failures, 3, marked = false).assertIsDisplayed()
    }

    @Test
    fun `tapping an empty point marks one more and a marked one removes one`() {
        val changes = mutableListOf<DeathSaves>()
        show(
            CharacterSheetUiState.Content(dying),
            combatActions = CombatActions(onDeathSavesChange = { changes += it }),
        )

        pip(R.string.death_save_successes, 3, marked = false).performClick()
        pip(R.string.death_save_failures, 1, marked = true).performClick()

        assertEquals(listOf(DeathSaves(successes = 2, failures = 2), DeathSaves(successes = 1, failures = 1)), changes)
    }

    @Test
    fun `with three failures the empty successes are disabled`() {
        show(CharacterSheetUiState.Content(dead()))

        pip(R.string.death_save_successes, 1, marked = false).assertIsNotEnabled()
    }

    @Test
    fun `the points can't be tapped while saving`() {
        show(CharacterSheetUiState.Content(dying, isSavingCombat = true))

        pip(R.string.death_save_successes, 1, marked = true).assertIsNotEnabled()
        pip(R.string.death_save_failures, 3, marked = false).assertIsNotEnabled()
    }

    @Test
    fun `a stable character says it doesn't roll`() {
        show(CharacterSheetUiState.Content(dying.copy(lifeStatus = LifeStatus.STABLE, deathSaves = DeathSaves(3, 1))))

        composeRule.onNodeWithText(context.getString(R.string.stable)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stable_message)).assertIsDisplayed()
    }

    @Test
    fun `a dead character shows it in the header and can be revived`() {
        var revived = false
        show(CharacterSheetUiState.Content(dead()), combatActions = CombatActions(onStartRevive = { revived = true }))

        // In the header and in the box.
        composeRule.onAllNodesWithText(context.getString(R.string.dead)).assertCountEquals(2)
        composeRule.onAllNodesWithText("0/44").assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.revive)).performClick()

        assertEquals(true, revived)
    }

    @Test
    fun `shows the error of a death save`() {
        show(CharacterSheetUiState.Content(dying, combatError = RepositoryError.Network))

        composeRule.onNodeWithText(
            context.getString(R.string.edit_save_error, context.getString(R.string.error_network)),
        ).assertIsDisplayed()
    }

    @Test
    fun `in edit mode tapping the armor class opens its editor`() {
        val started = mutableListOf<SheetEditTarget>()
        show(
            CharacterSheetUiState.Content(aldric, isEditing = true),
            SheetEditActions(onStartEdit = { started += it }),
        )

        composeRule.onNodeWithText(context.getString(R.string.combat_edit_hint)).assertIsDisplayed()
        composeRule.onNode(hasClickAction() and hasText("18")).performClick()

        assertEquals(listOf(SheetEditTarget.ARMOR_CLASS), started)
    }

    @Test
    fun `in read mode the armor class can't be edited`() {
        show(CharacterSheetUiState.Content(aldric))

        composeRule.onAllNodes(hasClickAction() and hasText("18")).assertCountEquals(0)
    }

    private fun dead(): Character =
        dying.copy(lifeStatus = LifeStatus.DEAD, deathSaves = DeathSaves(successes = 0, failures = 3))

    private fun show(
        uiState: CharacterSheetUiState,
        editActions: SheetEditActions = SheetEditActions(),
        combatActions: CombatActions = CombatActions(),
    ) {
        composeRule.setContent {
            CharacterSheetContent(
                uiState = uiState,
                onRetry = {},
                onSkillProficiencyChange = { _, _ -> },
                editActions = editActions,
                combatActions = combatActions,
            )
        }
    }
}
