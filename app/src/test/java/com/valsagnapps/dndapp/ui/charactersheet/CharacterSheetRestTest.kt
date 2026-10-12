package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.FakeCombatRepository
import com.valsagnapps.dndapp.data.sampleMonk
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CharacterSheetRestTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Monk 6 with 6 d8, 1 Ki spent (short rest), CON +2, 20/44 HP.
    private val tired = sampleMonk().copy(currentHitPoints = 20, hitDice = listOf(HitDice(8, 6)))
    private val repository = FakeCharacterRepository(listOf(tired))
    private val combatRepository = FakeCombatRepository(repository)

    // Lazy: the ViewModel loads on creation, which has to happen after the dispatcher rule.
    private val viewModel by lazy { CharacterSheetViewModel("1", repository, combatRepository) }

    private val content get() = viewModel.uiState.value as CharacterSheetUiState.Content

    @Test
    fun `the short rest opens with the hit dice and the hit points`() {
        viewModel.combat.onStartShortRest()

        assertEquals(
            CombatDialog.ShortRest(
                hitDice = listOf(HitDice(8, 6)),
                currentHitPoints = 20,
                maxHitPoints = 44,
                constitutionModifier = 2,
                canSpendDice = true,
            ),
            content.combat,
        )
    }

    @Test
    fun `resting spends the dice, heals and closes the sheet`() {
        viewModel.combat.onStartShortRest()
        viewModel.combat.onInput(CombatInput.SpentHitDice(8, 2))
        viewModel.combat.onInput(CombatInput.Digit(9))
        viewModel.combat.onConfirm()

        assertEquals(listOf("short rest 1 2d8 9"), combatRepository.calls)
        assertEquals(20 + 9 + 2 * 2, content.character.currentHitPoints)
        assertEquals(0, content.character.resources.first { it.id == "KI" }.used)
        assertNull(content.combat)
    }

    @Test
    fun `a stable character rests without spending dice`() {
        repository.characters[0] = tired.copy(
            currentHitPoints = 0,
            lifeStatus = LifeStatus.STABLE,
            deathSaves = DeathSaves(successes = 3),
        )

        viewModel.combat.onStartShortRest()

        assertEquals(false, (content.combat as CombatDialog.ShortRest).canSpendDice)
    }

    @Test
    fun `a dying character can't rest`() {
        repository.characters[0] = tired.copy(currentHitPoints = 0, lifeStatus = LifeStatus.DYING)

        viewModel.combat.onStartShortRest()
        viewModel.combat.onLongRest()

        assertNull(content.combat)
        assertEquals(emptyList<String>(), combatRepository.calls)
    }

    @Test
    fun `the long rest applies right away`() {
        viewModel.combat.onLongRest()

        assertEquals(listOf("long rest 1"), combatRepository.calls)
        assertEquals(44, content.character.currentHitPoints)
    }
}
