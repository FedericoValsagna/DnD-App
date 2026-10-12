package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.FakeCombatRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.deflectRow
import com.valsagnapps.dndapp.data.kiRow
import com.valsagnapps.dndapp.data.sampleMonk
import com.valsagnapps.dndapp.data.wholenessRow
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CharacterSheetResourcesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCharacterRepository(listOf(sampleMonk()))
    private val combatRepository = FakeCombatRepository(repository)

    // Lazy: the ViewModel loads on creation, which has to happen after the dispatcher rule.
    private val viewModel by lazy { CharacterSheetViewModel("1", repository, combatRepository) }

    private val content get() = viewModel.uiState.value as CharacterSheetUiState.Content

    private fun used(resource: String) = content.character.resources.first { it.id == resource }.used

    @Test
    fun `opening an action shows its resource as it is`() {
        viewModel.combat.onOpenAction(kiRow)

        assertEquals(CombatDialog.Action(kiRow), content.combat)
    }

    @Test
    fun `an action without uses opens without a resource`() {
        viewModel.combat.onOpenAction(deflectRow)

        assertEquals(CombatDialog.Action(deflectRow), content.combat)
    }

    @Test
    fun `spends from a pool with the keypad and closes the sheet`() {
        viewModel.combat.onOpenAction(kiRow)
        viewModel.combat.onInput(CombatInput.Digit(2))
        viewModel.combat.onConfirm()

        assertEquals(listOf("resource 1 KI 3"), combatRepository.calls)
        assertEquals(3, used("KI"))
        assertNull(content.combat)
    }

    @Test
    fun `recovers to a pool`() {
        viewModel.combat.onOpenAction(kiRow)
        viewModel.combat.onInput(CombatInput.Recovering(true))
        viewModel.combat.onInput(CombatInput.Digit(1))
        viewModel.combat.onConfirm()

        assertEquals(listOf("resource 1 KI 0"), combatRepository.calls)
    }

    @Test
    fun `more than what's left is not sent`() {
        viewModel.combat.onOpenAction(kiRow)
        viewModel.combat.onInput(CombatInput.Digit(6))
        viewModel.combat.onConfirm()

        assertEquals(emptyList<String>(), combatRepository.calls)
    }

    @Test
    fun `tapping a point saves the used uses right away`() {
        viewModel.combat.onResourceUsedChange("WHOLENESS_OF_BODY", 1)

        assertEquals(listOf("resource 1 WHOLENESS_OF_BODY 1"), combatRepository.calls)
        assertEquals(1, used("WHOLENESS_OF_BODY"))
    }

    @Test
    fun `a point in the action's sheet saves and closes it`() {
        viewModel.combat.onOpenAction(wholenessRow)
        viewModel.combat.onResourceUsedChange("WHOLENESS_OF_BODY", 1)

        assertEquals(1, used("WHOLENESS_OF_BODY"))
        assertNull(content.combat)
    }

    @Test
    fun `used uses out of range or of an unknown resource are not sent`() {
        viewModel.combat.onResourceUsedChange("WHOLENESS_OF_BODY", 2)
        viewModel.combat.onResourceUsedChange("KI", -1)
        viewModel.combat.onResourceUsedChange("RAGE", 1)

        assertEquals(emptyList<String>(), combatRepository.calls)
    }

    @Test
    fun `a failure keeps the sheet open with the error`() {
        viewModel.combat.onOpenAction(kiRow)
        repository.failWith = RepositoryError.Network
        viewModel.combat.onInput(CombatInput.Digit(1))
        viewModel.combat.onConfirm()

        assertEquals(RepositoryError.Network, content.combatError)
        assertEquals(CombatDialog.Action(kiRow, amount = "1"), content.combat)
    }
}
