package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.ui.common.toNumericInput

/** Values edited in the sheet's dialogs. Numbers are kept as text so the user can clear them while typing. */
sealed interface SheetEdit {
    val isValid: Boolean

    data class Class(val characterClass: CharacterClass?, val level: String) : SheetEdit {
        val isLevelValid: Boolean get() = level.toIntOrNull()?.let(CharacterRules::isValidLevel) == true
        override val isValid: Boolean get() = characterClass != null && isLevelValid
    }

    data class MaxHitPoints(val value: String) : SheetEdit {
        override val isValid: Boolean
            get() = value.toIntOrNull()?.let(CharacterRules::isValidMaxHitPoints) == true
    }
}

/** A change the user makes in one of the fields of an edit dialog. */
sealed interface SheetEditInput {
    data class ClassChange(val characterClass: CharacterClass) : SheetEditInput

    data class LevelChange(val level: String) : SheetEditInput

    data class MaxHitPointsChange(val value: String) : SheetEditInput
}

/** The edit with [input] applied. An input from another dialog leaves it as it is. */
fun SheetEdit.changedBy(input: SheetEditInput): SheetEdit = when (this) {
    is SheetEdit.Class -> when (input) {
        is SheetEditInput.ClassChange -> copy(characterClass = input.characterClass)
        is SheetEditInput.LevelChange -> copy(level = input.level.toNumericInput(CharacterRules.LEVEL_RANGE))
        else -> this
    }
    is SheetEdit.MaxHitPoints -> when (input) {
        is SheetEditInput.MaxHitPointsChange ->
            copy(value = input.value.toNumericInput(CharacterRules.MAX_HIT_POINTS_RANGE))
        else -> this
    }
}
