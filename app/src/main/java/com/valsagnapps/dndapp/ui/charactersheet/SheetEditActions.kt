package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterClass

/** Callbacks of the sheet's edit dialogs, grouped to keep the screen's signature short. */
class SheetEditActions(
    val onEditClass: () -> Unit = {},
    val onEditMaxHitPoints: () -> Unit = {},
    val onClassChange: (CharacterClass) -> Unit = {},
    val onLevelChange: (String) -> Unit = {},
    val onMaxHitPointsChange: (String) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
