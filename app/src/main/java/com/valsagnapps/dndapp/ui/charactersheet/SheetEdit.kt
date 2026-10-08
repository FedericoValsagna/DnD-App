package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.Subclass
import com.valsagnapps.dndapp.ui.common.toNumericInput

/** Values edited in the sheet's dialogs. Numbers are kept as text so the user can clear them while typing. */
sealed interface SheetEdit {
    val isValid: Boolean

    /**
     * [subclass] is kept while typing the level; what gets saved is [subclassToSave]. [catalog] offers the
     * subclasses: if it's empty (it couldn't be loaded) the subclass can't be changed.
     */
    data class Class(
        val characterClass: CharacterClass?,
        val level: String,
        val subclass: Subclass? = null,
        val catalog: List<ClassInfo> = emptyList(),
    ) : SheetEdit {
        val isLevelValid: Boolean get() = level.toIntOrNull()?.let(CharacterRules::isValidLevel) == true
        override val isValid: Boolean get() = characterClass != null && isLevelValid

        private val classInfo: ClassInfo? get() = catalog.find { it.characterClass == characterClass }

        val subclassOptions: List<Subclass> get() = classInfo?.subclasses.orEmpty()

        /** Class level to choose the subclass, or null if the catalog doesn't have the class. */
        val subclassLevel: Int? get() = classInfo?.subclassLevel

        val canChooseSubclass: Boolean
            get() {
                val level = level.toIntOrNull()
                val subclassLevel = subclassLevel
                return level != null && subclassLevel != null && level >= subclassLevel
            }

        /** The chosen subclass, only if the level allows having one. */
        val subclassToSave: Subclass? get() = subclass.takeIf { canChooseSubclass }
    }

    data class MaxHitPoints(val value: String) : SheetEdit {
        override val isValid: Boolean
            get() = value.toIntOrNull()?.let(CharacterRules::isValidMaxHitPoints) == true
    }
}

/** A change the user makes in one of the fields of an edit dialog. */
sealed interface SheetEditInput {
    data class ClassChange(val characterClass: CharacterClass) : SheetEditInput

    /** Null to have no subclass. */
    data class SubclassChange(val subclass: Subclass?) : SheetEditInput

    data class LevelChange(val level: String) : SheetEditInput

    data class MaxHitPointsChange(val value: String) : SheetEditInput
}

/** The edit with [input] applied. An input from another dialog leaves it as it is. */
fun SheetEdit.changedBy(input: SheetEditInput): SheetEdit = when (this) {
    is SheetEdit.Class -> when (input) {
        // The subclass belongs to the class: another class starts without one.
        is SheetEditInput.ClassChange -> copy(
            characterClass = input.characterClass,
            subclass = subclass.takeIf { input.characterClass == characterClass },
        )
        is SheetEditInput.SubclassChange -> copy(subclass = input.subclass)
        is SheetEditInput.LevelChange -> copy(level = input.level.toNumericInput(CharacterRules.LEVEL_RANGE))
        else -> this
    }
    is SheetEdit.MaxHitPoints -> when (input) {
        is SheetEditInput.MaxHitPointsChange ->
            copy(value = input.value.toNumericInput(CharacterRules.MAX_HIT_POINTS_RANGE))
        else -> this
    }
}
