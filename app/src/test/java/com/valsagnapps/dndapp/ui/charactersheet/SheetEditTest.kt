package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterClass
import org.junit.Assert.assertEquals
import org.junit.Test

class SheetEditTest {

    @Test
    fun `class inputs change the class and the level`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "5")

        assertEquals(
            SheetEdit.Class(CharacterClass.WIZARD, "5"),
            edit.changedBy(SheetEditInput.ClassChange(CharacterClass.WIZARD)),
        )
        assertEquals(SheetEdit.Class(CharacterClass.FIGHTER, "12"), edit.changedBy(SheetEditInput.LevelChange("1a2")))
    }

    @Test
    fun `max hit points input keeps only digits within range`() {
        assertEquals(
            SheetEdit.MaxHitPoints("472"),
            SheetEdit.MaxHitPoints("44").changedBy(SheetEditInput.MaxHitPointsChange("4a72")),
        )
    }

    @Test
    fun `an input from another dialog leaves the edit as it is`() {
        val classEdit = SheetEdit.Class(CharacterClass.FIGHTER, "5")
        val hitPointsEdit = SheetEdit.MaxHitPoints("44")

        assertEquals(classEdit, classEdit.changedBy(SheetEditInput.MaxHitPointsChange("12")))
        assertEquals(hitPointsEdit, hitPointsEdit.changedBy(SheetEditInput.LevelChange("3")))
    }
}
