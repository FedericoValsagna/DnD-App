package com.valsagnapps.dndapp.ui.common

import androidx.annotation.StringRes
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.CharacterClass

@StringRes
fun CharacterClass.nameRes(): Int = when (this) {
    CharacterClass.BARBARIAN -> R.string.class_barbarian
    CharacterClass.BARD -> R.string.class_bard
    CharacterClass.CLERIC -> R.string.class_cleric
    CharacterClass.DRUID -> R.string.class_druid
    CharacterClass.FIGHTER -> R.string.class_fighter
    CharacterClass.MONK -> R.string.class_monk
    CharacterClass.PALADIN -> R.string.class_paladin
    CharacterClass.RANGER -> R.string.class_ranger
    CharacterClass.ROGUE -> R.string.class_rogue
    CharacterClass.SORCERER -> R.string.class_sorcerer
    CharacterClass.WARLOCK -> R.string.class_warlock
    CharacterClass.WIZARD -> R.string.class_wizard
}
