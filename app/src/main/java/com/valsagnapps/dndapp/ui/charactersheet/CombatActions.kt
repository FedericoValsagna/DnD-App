package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.DeathSaves

/** Callbacks of the combat actions ([SheetCombat]), grouped as [SheetEditActions]. */
class CombatActions(
    /** Tapping the HP in the header. */
    val onOpenHitPoints: () -> Unit = {},
    val onStartRevive: () -> Unit = {},
    val onInput: (CombatInput) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
    /** The whole counters after tapping a point. */
    val onDeathSavesChange: (DeathSaves) -> Unit = {},
    /** Tapping an action's row in Combate. */
    val onOpenFeature: (ClassFeature) -> Unit = {},
    /** The used uses of a resource (by id) after tapping one of its points. */
    val onResourceUsedChange: (String, Int) -> Unit = { _, _ -> },
)
