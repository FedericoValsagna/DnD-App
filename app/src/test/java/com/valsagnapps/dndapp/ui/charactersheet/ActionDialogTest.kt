package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.deflectRow
import com.valsagnapps.dndapp.data.kiRow
import com.valsagnapps.dndapp.data.wholenessRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionDialogTest {

    private fun CombatDialog.Action.with(vararg inputs: CombatInput): CombatDialog.Action =
        inputs.fold(this as CombatDialog) { dialog, input -> dialog.changedBy(input) } as CombatDialog.Action

    private val ki = CombatDialog.Action(kiRow)

    @Test
    fun `spending from a pool adds to the used uses`() {
        val dialog = ki.with(CombatInput.Digit(3))

        assertTrue(dialog.isValid)
        assertEquals(4, dialog.newUsed)
    }

    @Test
    fun `a pool spends at most what's left and recovers at most what's spent`() {
        assertFalse(ki.with(CombatInput.Digit(6)).isValid)
        assertTrue(ki.with(CombatInput.Digit(5)).isValid)
        assertFalse(ki.with(CombatInput.Recovering(true), CombatInput.Digit(2)).isValid)
        assertEquals(0, ki.with(CombatInput.Recovering(true), CombatInput.Digit(1)).newUsed)
    }

    @Test
    fun `zero is not an amount`() {
        assertFalse(ki.with(CombatInput.Digit(0)).isValid)
        assertNull(ki.newUsed)
    }

    @Test
    fun `the amount has at most the digits of the max`() {
        assertEquals("1", ki.with(CombatInput.Digit(1), CombatInput.Digit(2)).amount)
        assertEquals("", ki.with(CombatInput.Digit(4), CombatInput.Backspace).amount)
        assertEquals("", ki.with(CombatInput.Digit(4), CombatInput.Clear).amount)
    }

    @Test
    fun `points and actions without uses have nothing to confirm`() {
        val points = CombatDialog.Action(wholenessRow).with(CombatInput.Digit(1))

        assertFalse(points.isPool)
        assertFalse(points.isValid)
        assertFalse(CombatDialog.Action(deflectRow).with(CombatInput.Digit(1)).isValid)
        assertEquals("", CombatDialog.Action(deflectRow).with(CombatInput.Digit(1)).amount)
    }

    @Test
    fun `hit points inputs leave the sheet as it is`() {
        assertEquals(ki, ki.with(CombatInput.Critical(true), CombatInput.ReviveHitPoints("3")))
    }
}
