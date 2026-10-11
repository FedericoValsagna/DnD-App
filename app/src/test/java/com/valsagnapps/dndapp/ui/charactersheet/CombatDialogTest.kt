package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.HitPointsAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CombatDialogTest {

    private fun typed(vararg digits: Int, action: HitPointsAction = HitPointsAction.DAMAGE): CombatDialog.HitPoints =
        digits.fold(CombatDialog.HitPoints(action) as CombatDialog) { dialog, digit ->
            dialog.changedBy(CombatInput.Digit(digit))
        } as CombatDialog.HitPoints

    @Test
    fun `the keypad types the amount`() {
        assertEquals("128", typed(1, 2, 8).amount)
        assertEquals(128, typed(1, 2, 8).amountValue)
    }

    @Test
    fun `leading zeros are dropped`() {
        assertEquals("5", typed(0, 0, 5).amount)
    }

    @Test
    fun `damage starts at 1 and temporary hit points at 0`() {
        assertFalse(typed(0).isValid)
        assertFalse(CombatDialog.HitPoints().isValid)
        assertTrue(typed(0, action = HitPointsAction.TEMPORARY).isValid)
    }

    @Test
    fun `the amount stops at the digits of the largest value`() {
        assertEquals("9999", typed(9, 9, 9, 9, 9).amount)
        assertEquals("999", typed(9, 9, 9, 9, action = HitPointsAction.TEMPORARY).amount)
    }

    @Test
    fun `changing the action keeps the amount as long as it fits`() {
        val dialog = typed(1, 2, 3, 4).changedBy(CombatInput.Action(HitPointsAction.TEMPORARY))

        assertEquals(CombatDialog.HitPoints(HitPointsAction.TEMPORARY, "123"), dialog)
    }

    @Test
    fun `backspace removes the last digit and clear removes them all`() {
        assertEquals("12", (typed(1, 2, 8).changedBy(CombatInput.Backspace) as CombatDialog.HitPoints).amount)
        assertEquals("", (typed(1, 2, 8).changedBy(CombatInput.Clear) as CombatDialog.HitPoints).amount)
        assertNull((typed().changedBy(CombatInput.Backspace) as CombatDialog.HitPoints).amountValue)
    }

    @Test
    fun `marks the hit as critical`() {
        assertTrue((typed(5).changedBy(CombatInput.Critical(true)) as CombatDialog.HitPoints).critical)
    }

    @Test
    fun `reviving takes 1 to the max hit points`() {
        val dialog = CombatDialog.Revive(maxHitPoints = 44)

        assertTrue(dialog.isValid)
        assertEquals("44", (dialog.changedBy(CombatInput.ReviveHitPoints("44")) as CombatDialog.Revive).hitPoints)
        assertFalse((dialog.changedBy(CombatInput.ReviveHitPoints("45")) as CombatDialog.Revive).isValid)
        assertFalse((dialog.changedBy(CombatInput.ReviveHitPoints("0")) as CombatDialog.Revive).isValid)
    }

    @Test
    fun `an input from the other dialog leaves it as it is`() {
        val revive = CombatDialog.Revive(maxHitPoints = 44)

        assertEquals(revive, revive.changedBy(CombatInput.Digit(3)))
        assertEquals(typed(3), typed(3).changedBy(CombatInput.ReviveHitPoints("9")))
    }

    @Test
    fun `the armor class can be left empty to use the calculated one`() {
        assertTrue(SheetEdit.ArmorClass("").isValid)
        assertNull(SheetEdit.ArmorClass("").override)
        assertEquals(18, SheetEdit.ArmorClass("18").override)
        assertFalse(SheetEdit.ArmorClass("0").isValid)
        assertEquals(
            "50",
            (SheetEdit.ArmorClass("").changedBy(SheetEditInput.ArmorClassChange("500")) as SheetEdit.ArmorClass).value,
        )
    }

    @Test
    fun `the base speed goes from 0 to 200`() {
        assertTrue(SheetEdit.Speed("0").isValid)
        assertFalse(SheetEdit.Speed("").isValid)
        assertFalse(SheetEdit.Speed("201").isValid)
        assertEquals("25", (SheetEdit.Speed("30").changedBy(SheetEditInput.SpeedChange("25")) as SheetEdit.Speed).value)
    }
}
