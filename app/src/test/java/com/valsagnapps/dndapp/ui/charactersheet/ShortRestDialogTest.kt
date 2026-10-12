package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.HitDice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortRestDialogTest {

    private fun CombatDialog.ShortRest.with(vararg inputs: CombatInput): CombatDialog.ShortRest =
        inputs.fold(this as CombatDialog) { dialog, input -> dialog.changedBy(input) } as CombatDialog.ShortRest

    // 4 d10 and 3 d8 left, CON +2, 20/44 HP.
    private val rest = CombatDialog.ShortRest(
        hitDice = listOf(HitDice(10, 5, used = 1), HitDice(8, 3)),
        currentHitPoints = 20,
        maxHitPoints = 44,
        constitutionModifier = 2,
        canSpendDice = true,
    )

    @Test
    fun `without dice it rests without a roll`() {
        assertTrue(rest.isValid)
        assertEquals(0, rest.rollValue)
        assertEquals(20, rest.hitPointsAfter)
    }

    @Test
    fun `with dice the roll is needed and must be possible`() {
        val twoD10 = rest.with(CombatInput.SpentHitDice(10, 2))

        assertFalse(twoD10.isValid)
        assertFalse(twoD10.with(CombatInput.Digit(1)).isValid)
        assertFalse(twoD10.with(CombatInput.Digit(2), CombatInput.Digit(1)).isValid)
        assertTrue(twoD10.with(CombatInput.Digit(2), CombatInput.Digit(0)).isValid)
    }

    @Test
    fun `the hit points after add the constitution per die, up to the max`() {
        val twoD10 = rest.with(CombatInput.SpentHitDice(10, 2), CombatInput.Digit(9))

        assertEquals(20 + 9 + 2 * 2, twoD10.hitPointsAfter)
        val all = twoD10.with(CombatInput.SpentHitDice(8, 3), CombatInput.Clear, CombatInput.Digit(4))
        assertEquals(44, all.with(CombatInput.Digit(0)).hitPointsAfter)
    }

    @Test
    fun `a negative constitution doesn't take hit points`() {
        val weak = rest.copy(constitutionModifier = -3).with(CombatInput.SpentHitDice(8, 1), CombatInput.Digit(1))

        assertEquals(20, weak.hitPointsAfter)
    }

    @Test
    fun `the dice go from 0 to the ones left`() {
        assertEquals(4, rest.with(CombatInput.SpentHitDice(10, 4)).spentOf(10))
        assertEquals(0, rest.with(CombatInput.SpentHitDice(10, 5)).spentOf(10))
        assertEquals(0, rest.with(CombatInput.SpentHitDice(10, -1)).spentOf(10))
        assertEquals(0, rest.with(CombatInput.SpentHitDice(6, 1)).spentOf(6))
    }

    @Test
    fun `at 0 hit points no dice can be spent`() {
        val stable = rest.copy(currentHitPoints = 0, canSpendDice = false)

        assertEquals(0, stable.with(CombatInput.SpentHitDice(10, 1)).diceCount)
    }

    @Test
    fun `an impossible roll has no value`() {
        assertNull(rest.with(CombatInput.SpentHitDice(8, 1), CombatInput.Digit(9)).rollValue)
    }
}
