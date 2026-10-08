package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.Subclass

/** Callbacks of the sheet's edit mode and dialogs, grouped to keep the screen's signature short. */
class SheetEditActions(
    val onToggleEditing: () -> Unit = {},
    val onEditClass: () -> Unit = {},
    val onEditMaxHitPoints: () -> Unit = {},
    val onClassChange: (CharacterClass) -> Unit = {},
    val onLevelChange: (String) -> Unit = {},
    val onSubclassChange: (Subclass?) -> Unit = {},
    val onMaxHitPointsChange: (String) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
