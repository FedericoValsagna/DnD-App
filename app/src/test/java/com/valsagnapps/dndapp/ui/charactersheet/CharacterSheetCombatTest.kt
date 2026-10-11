package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.FakeCombatRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.HitPointsAction
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CharacterSheetCombatTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val aldric = sampleCharacter(id = "1", name = "Aldric").copy(
        maxHitPoints = 44,
        currentHitPoints = 12,
        temporaryHitPoints = 5,
        armorClass = 16,
        baseSpeed = 30,
        speed = 30,
    )
    private val dying = aldric.copy(currentHitPoints = 0, temporaryHitPoints = 0, lifeStatus = LifeStatus.DYING)
    private val dead = dying.copy(lifeStatus = LifeStatus.DEAD, deathSaves = DeathSaves(failures = 3))

    private lateinit var repository: FakeCharacterRepository
    private lateinit var combatRepository: FakeCombatRepository

    private fun viewModel(character: com.valsagnapps.dndapp.domain.Character = aldric): CharacterSheetViewModel {
        repository = FakeCharacterRepository(listOf(character))
        combatRepository = FakeCombatRepository(repository)
        return CharacterSheetViewModel("1", repository, combatRepository)
    }

    private val CharacterSheetViewModel.content get() = uiState.value as CharacterSheetUiState.Content

    private fun CharacterSheetViewModel.type(vararg inputs: CombatInput) = inputs.forEach(combat::onInput)

    @Test
    fun `applies damage from the HP sheet and closes it`() {
        val viewModel = viewModel()

        viewModel.combat.onOpenHitPoints()
        viewModel.type(CombatInput.Digit(8))
        viewModel.combat.onConfirm()

        assertEquals(listOf("damage 1 8 critical=false"), combatRepository.calls)
        assertEquals(9, viewModel.content.character.currentHitPoints)
        assertEquals(0, viewModel.content.character.temporaryHitPoints)
        assertNull(viewModel.content.combat)
    }

    @Test
    fun `heals and sets temporary hit points`() {
        val viewModel = viewModel()

        viewModel.combat.onOpenHitPoints()
        viewModel.type(CombatInput.Action(HitPointsAction.HEALING), CombatInput.Digit(5))
        viewModel.combat.onConfirm()
        viewModel.combat.onOpenHitPoints()
        viewModel.type(CombatInput.Action(HitPointsAction.TEMPORARY), CombatInput.Digit(0))
        viewModel.combat.onConfirm()

        assertEquals(listOf("heal 1 5", "temporary 1 0"), combatRepository.calls)
        assertEquals(17, viewModel.content.character.currentHitPoints)
    }

    @Test
    fun `sends the critical hit at 0 hit points`() {
        val viewModel = viewModel(dying)

        viewModel.combat.onOpenHitPoints()
        viewModel.type(CombatInput.Digit(3), CombatInput.Critical(true))
        viewModel.combat.onConfirm()

        assertEquals(listOf("damage 1 3 critical=true"), combatRepository.calls)
        assertEquals(DeathSaves(failures = 2), viewModel.content.character.deathSaves)
    }

    @Test
    fun `an invalid amount is not sent`() {
        val viewModel = viewModel()

        viewModel.combat.onOpenHitPoints()
        viewModel.combat.onConfirm()

        assertTrue(combatRepository.calls.isEmpty())
        assertEquals(CombatDialog.HitPoints(), viewModel.content.combat)
    }

    @Test
    fun `a failed action keeps the sheet open with the error`() {
        val viewModel = viewModel()
        viewModel.combat.onOpenHitPoints()
        viewModel.type(CombatInput.Digit(8))
        repository.failWith = RepositoryError.Network

        viewModel.combat.onConfirm()

        assertEquals(RepositoryError.Network, viewModel.content.combatError)
        assertEquals(CombatDialog.HitPoints(amount = "8"), viewModel.content.combat)
        assertEquals(false, viewModel.content.isSavingCombat)
    }

    @Test
    fun `the sheet can't be closed or changed while saving`() {
        val viewModel = viewModel()
        viewModel.combat.onOpenHitPoints()
        viewModel.type(CombatInput.Digit(8))
        repository.gate = CompletableDeferred()

        viewModel.combat.onConfirm()
        viewModel.type(CombatInput.Digit(1))
        viewModel.combat.onDismiss()

        assertTrue(viewModel.content.isSavingCombat)
        assertEquals(CombatDialog.HitPoints(amount = "8"), viewModel.content.combat)
        repository.gate!!.complete(Unit)
        assertNull(viewModel.content.combat)
    }

    @Test
    fun `a dead character's HP sheet doesn't open`() {
        val viewModel = viewModel(dead)

        viewModel.combat.onOpenHitPoints()

        assertNull(viewModel.content.combat)
    }

    @Test
    fun `marking a death save saves the counters right away`() {
        val viewModel = viewModel(dying)

        viewModel.combat.onDeathSavesChange(DeathSaves(successes = 1))

        assertEquals(listOf("death saves 1 1/0"), combatRepository.calls)
        assertEquals(DeathSaves(successes = 1), viewModel.content.character.deathSaves)
    }

    @Test
    fun `death saves need 0 hit points`() {
        val viewModel = viewModel()

        viewModel.combat.onDeathSavesChange(DeathSaves(successes = 1))

        assertTrue(combatRepository.calls.isEmpty())
    }

    @Test
    fun `a failed death save shows the error without a dialog`() {
        val viewModel = viewModel(dying)
        repository.failWith = RepositoryError.Network

        viewModel.combat.onDeathSavesChange(DeathSaves(failures = 1))

        assertEquals(RepositoryError.Network, viewModel.content.combatError)
        assertNull(viewModel.content.combat)
    }

    @Test
    fun `revives a dead character with the hit points chosen`() {
        val viewModel = viewModel(dead)

        viewModel.combat.onStartRevive()
        assertEquals(CombatDialog.Revive(maxHitPoints = 44), viewModel.content.combat)
        viewModel.type(CombatInput.ReviveHitPoints("44"))
        viewModel.combat.onConfirm()

        assertEquals(listOf("revive 1 44"), combatRepository.calls)
        assertEquals(LifeStatus.CONSCIOUS, viewModel.content.character.lifeStatus)
        assertNull(viewModel.content.combat)
    }

    @Test
    fun `only the dead are revived`() {
        val viewModel = viewModel()

        viewModel.combat.onStartRevive()

        assertNull(viewModel.content.combat)
    }

    @Test
    fun `combat dialogs don't open over an edit dialog`() {
        val viewModel = viewModel()
        viewModel.onToggleEditing()
        viewModel.onStartEdit(SheetEditTarget.NAME)

        viewModel.combat.onOpenHitPoints()

        assertNull(viewModel.content.combat)
    }

    @Test
    fun `saves the armor class override and removes it`() {
        val viewModel = viewModel()
        viewModel.onToggleEditing()

        viewModel.onStartEdit(SheetEditTarget.ARMOR_CLASS)
        assertEquals(SheetEdit.ArmorClass("", calculated = 16), viewModel.content.edit)
        viewModel.onEditInput(SheetEditInput.ArmorClassChange("18"))
        viewModel.onConfirmEdit()
        assertEquals(18, viewModel.content.character.armorClass)

        viewModel.onStartEdit(SheetEditTarget.ARMOR_CLASS)
        assertEquals(SheetEdit.ArmorClass("18", calculated = null), viewModel.content.edit)
        viewModel.onEditInput(SheetEditInput.ArmorClassChange(""))
        viewModel.onConfirmEdit()

        assertEquals(listOf("armor class 1 18", "armor class 1 null"), combatRepository.calls)
        assertEquals(FakeCombatRepository.CALCULATED_AC, viewModel.content.character.armorClass)
    }

    @Test
    fun `saves the base speed`() {
        val viewModel = viewModel()
        viewModel.onToggleEditing()

        viewModel.onStartEdit(SheetEditTarget.SPEED)
        assertEquals(SheetEdit.Speed("30"), viewModel.content.edit)
        viewModel.onEditInput(SheetEditInput.SpeedChange("25"))
        viewModel.onConfirmEdit()

        assertEquals(listOf("speed 1 25"), combatRepository.calls)
        assertEquals(25, viewModel.content.character.baseSpeed)
    }

    @Test
    fun `an older server without armor class or speed has nothing to edit`() {
        val viewModel = viewModel(sampleCharacter(id = "1"))
        viewModel.onToggleEditing()

        viewModel.onStartEdit(SheetEditTarget.ARMOR_CLASS)
        viewModel.onStartEdit(SheetEditTarget.SPEED)

        assertNull(viewModel.content.edit)
    }
}
