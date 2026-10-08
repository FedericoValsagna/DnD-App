package com.valsagnapps.dndapp.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.HitDice

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

/** E.g. "Cleric 15 (Life Domain)", or "Fighter 3 / Cleric 2" for a multiclass without subclasses. */
@Composable
fun classSummary(classes: List<ClassLevel>): String = if (classes.isEmpty()) {
    stringResource(R.string.missing_value)
} else {
    classes.map { classLevel ->
        val name = stringResource(classLevel.characterClass.nameRes())
        classLevel.subclass?.let { stringResource(R.string.class_level_subclass, name, classLevel.level, it.name) }
            ?: stringResource(R.string.class_level, name, classLevel.level)
    }.joinToString(stringResource(R.string.class_separator))
}

/** E.g. "15d8", or "3d10 + 2d8" for a multiclass. */
@Composable
fun hitDiceSummary(hitDice: List<HitDice>): String = if (hitDice.isEmpty()) {
    stringResource(R.string.missing_value)
} else {
    hitDice.map { stringResource(R.string.hit_dice_value, it.count, it.die) }
        .joinToString(stringResource(R.string.hit_dice_separator))
}
