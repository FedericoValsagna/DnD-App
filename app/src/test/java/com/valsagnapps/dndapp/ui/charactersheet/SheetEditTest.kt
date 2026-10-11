package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.battleMaster
import com.valsagnapps.dndapp.data.champion
import com.valsagnapps.dndapp.data.sampleCatalog
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `offers the subclasses of the chosen class from its subclass level`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "3", catalog = sampleCatalog())

        assertEquals(listOf(champion, battleMaster), edit.subclassOptions)
        assertEquals(3, edit.subclassLevel)
        assertTrue(edit.canChooseSubclass)
        assertFalse(edit.copy(level = "2").canChooseSubclass)
        assertFalse(edit.copy(level = "").canChooseSubclass)
    }

    @Test
    fun `cannot choose a subclass without the catalog`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", subclass = champion)

        assertEquals(emptyList<Any>(), edit.subclassOptions)
        assertNull(edit.subclassLevel)
        assertFalse(edit.canChooseSubclass)
    }

    @Test
    fun `keeps the subclass while typing the level but saves it only with the level for it`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", subclass = champion, catalog = sampleCatalog())

        val lowered = edit.changedBy(SheetEditInput.LevelChange("1"))
        assertEquals(champion, (lowered as SheetEdit.Class).subclass)
        assertNull((lowered as SheetEdit.Class).subclassToSave)

        val raised = lowered.changedBy(SheetEditInput.LevelChange("4")) as SheetEdit.Class
        assertEquals(champion, raised.subclassToSave)
    }

    @Test
    fun `changing the class drops the subclass`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", subclass = champion, catalog = sampleCatalog())

        assertEquals(
            champion,
            (edit.changedBy(SheetEditInput.ClassChange(CharacterClass.FIGHTER)) as SheetEdit.Class).subclass,
        )
        assertNull((edit.changedBy(SheetEditInput.ClassChange(CharacterClass.CLERIC)) as SheetEdit.Class).subclass)
    }

    @Test
    fun `chooses or removes the subclass`() {
        val edit = SheetEdit.Class(CharacterClass.FIGHTER, "5", catalog = sampleCatalog())

        val chosen = edit.changedBy(SheetEditInput.SubclassChange(battleMaster)) as SheetEdit.Class
        assertEquals(battleMaster, chosen.subclass)
        assertNull((chosen.changedBy(SheetEditInput.SubclassChange(null)) as SheetEdit.Class).subclass)
    }

    @Test
    fun `max hit points input keeps only digits within range`() {
        assertEquals(
            SheetEdit.MaxHitPoints("472"),
            SheetEdit.MaxHitPoints("44").changedBy(SheetEditInput.MaxHitPointsChange("4a72")),
        )
    }

    @Test
    fun `the name is valid without the surrounding spaces and up to the max length`() {
        assertTrue(SheetEdit.Name(" Tordek ").isValid)
        assertFalse(SheetEdit.Name("  ").isValid)
        assertEquals(
            SheetEdit.Name("a".repeat(100)),
            SheetEdit.Name("").changedBy(SheetEditInput.NameChange("a".repeat(101))),
        )
    }

    @Test
    fun `ability score inputs keep only digits and change only that ability`() {
        val edit = SheetEdit.AbilityScores(Ability.entries.associateWith { "10" })

        val changed = edit.changedBy(
            SheetEditInput.AbilityScoreChange(Ability.DEXTERITY, "1x45"),
        ) as SheetEdit.AbilityScores

        assertEquals(Ability.entries.associateWith { "10" } + (Ability.DEXTERITY to "14"), changed.values)
        assertTrue(changed.isValid)
        assertEquals(14, changed.toScores()[Ability.DEXTERITY])
    }

    @Test
    fun `ability scores are valid only with the six within range`() {
        val valid = Ability.entries.associateWith { "10" }

        assertFalse(SheetEdit.AbilityScores(valid + (Ability.WISDOM to "0")).isValid(Ability.WISDOM))
        assertFalse(SheetEdit.AbilityScores(valid + (Ability.WISDOM to "31")).isValid)
        assertFalse(SheetEdit.AbilityScores(valid - Ability.WISDOM).isValid)
        assertTrue(SheetEdit.AbilityScores(valid + (Ability.WISDOM to "30")).isValid)
    }

    @Test
    fun `the delete confirmation is always valid and ignores inputs`() {
        val edit = SheetEdit.DeleteCharacter("Tordek")

        assertTrue(edit.isValid)
        assertEquals(edit, edit.changedBy(SheetEditInput.NameChange("Krusk")))
    }

    @Test
    fun `an input from another dialog leaves the edit as it is`() {
        val classEdit = SheetEdit.Class(CharacterClass.FIGHTER, "5")
        val hitPointsEdit = SheetEdit.MaxHitPoints("44")

        assertEquals(classEdit, classEdit.changedBy(SheetEditInput.MaxHitPointsChange("12")))
        assertEquals(hitPointsEdit, hitPointsEdit.changedBy(SheetEditInput.LevelChange("3")))
        assertEquals(SheetEdit.Name("A"), SheetEdit.Name("A").changedBy(SheetEditInput.MaxHitPointsChange("1")))
        val scores = SheetEdit.AbilityScores(emptyMap())
        assertEquals(scores, scores.changedBy(SheetEditInput.NameChange("A")))
    }
}
