package com.valsagnapps.dndapp.ui.common

import androidx.annotation.StringRes
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Ability

@StringRes
fun Ability.nameRes(): Int = when (this) {
    Ability.STRENGTH -> R.string.ability_strength
    Ability.DEXTERITY -> R.string.ability_dexterity
    Ability.CONSTITUTION -> R.string.ability_constitution
    Ability.INTELLIGENCE -> R.string.ability_intelligence
    Ability.WISDOM -> R.string.ability_wisdom
    Ability.CHARISMA -> R.string.ability_charisma
}

@StringRes
fun Ability.shortNameRes(): Int = when (this) {
    Ability.STRENGTH -> R.string.ability_strength_short
    Ability.DEXTERITY -> R.string.ability_dexterity_short
    Ability.CONSTITUTION -> R.string.ability_constitution_short
    Ability.INTELLIGENCE -> R.string.ability_intelligence_short
    Ability.WISDOM -> R.string.ability_wisdom_short
    Ability.CHARISMA -> R.string.ability_charisma_short
}
