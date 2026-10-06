package com.valsagnapps.dndapp.ui.common

import androidx.annotation.StringRes
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.ArmorProficiency
import com.valsagnapps.dndapp.domain.ToolCategory
import com.valsagnapps.dndapp.domain.ToolProficiency
import com.valsagnapps.dndapp.domain.WeaponProficiency

@StringRes
fun ArmorProficiency.nameRes(): Int = when (this) {
    ArmorProficiency.LIGHT -> R.string.armor_light
    ArmorProficiency.MEDIUM -> R.string.armor_medium
    ArmorProficiency.HEAVY -> R.string.armor_heavy
    ArmorProficiency.SHIELDS -> R.string.armor_shields
}

@StringRes
fun WeaponProficiency.nameRes(): Int = when (this) {
    WeaponProficiency.SIMPLE -> R.string.weapon_simple
    WeaponProficiency.MARTIAL -> R.string.weapon_martial
    WeaponProficiency.CLUB -> R.string.weapon_club
    WeaponProficiency.DAGGER -> R.string.weapon_dagger
    WeaponProficiency.DART -> R.string.weapon_dart
    WeaponProficiency.JAVELIN -> R.string.weapon_javelin
    WeaponProficiency.LIGHT_CROSSBOW -> R.string.weapon_light_crossbow
    WeaponProficiency.MACE -> R.string.weapon_mace
    WeaponProficiency.QUARTERSTAFF -> R.string.weapon_quarterstaff
    WeaponProficiency.SICKLE -> R.string.weapon_sickle
    WeaponProficiency.SLING -> R.string.weapon_sling
    WeaponProficiency.SPEAR -> R.string.weapon_spear
    WeaponProficiency.HAND_CROSSBOW -> R.string.weapon_hand_crossbow
    WeaponProficiency.LONGSWORD -> R.string.weapon_longsword
    WeaponProficiency.RAPIER -> R.string.weapon_rapier
    WeaponProficiency.SCIMITAR -> R.string.weapon_scimitar
    WeaponProficiency.SHORTSWORD -> R.string.weapon_shortsword
}

@StringRes
fun ToolProficiency.nameRes(): Int = when (this) {
    ToolProficiency.HERBALISM_KIT -> R.string.tool_herbalism_kit
    ToolProficiency.THIEVES_TOOLS -> R.string.tool_thieves_tools
}

@StringRes
fun ToolCategory.nameRes(): Int = when (this) {
    ToolCategory.ARTISANS_TOOLS -> R.string.tool_category_artisans_tools
    ToolCategory.MUSICAL_INSTRUMENT -> R.string.tool_category_musical_instrument
}
