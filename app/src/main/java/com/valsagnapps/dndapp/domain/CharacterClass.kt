package com.valsagnapps.dndapp.domain

/** The PHB (2014) classes, same as the server. */
enum class CharacterClass {
    BARBARIAN,
    BARD,
    CLERIC,
    DRUID,
    FIGHTER,
    MONK,
    PALADIN,
    RANGER,
    ROGUE,
    SORCERER,
    WARLOCK,
    WIZARD,
}

data class ClassLevel(val characterClass: CharacterClass, val level: Int)
