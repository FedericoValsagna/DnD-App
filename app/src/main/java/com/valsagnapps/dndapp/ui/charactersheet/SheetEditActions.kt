package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.Subclass

/** Callbacks of the sheet's edit mode and dialogs, grouped to keep the screen's signature short. */
class SheetEditActions(
    val onToggleEditing: () -> Unit = {},
    /** Opens a dialog: the name, the class, the max HP, the abilities, or the confirmation to delete. */
    val onStartEdit: (SheetEditTarget) -> Unit = {},
    val onClassChange: (CharacterClass) -> Unit = {},
    val onLevelChange: (String) -> Unit = {},
    val onSubclassChange: (Subclass?) -> Unit = {},
    val onMaxHitPointsChange: (String) -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onAbilityScoreChange: (Ability, String) -> Unit = { _, _ -> },
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
