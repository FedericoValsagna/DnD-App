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

/** [subclass] is null if none was chosen (a character may not have one even at the level to choose it). */
data class ClassLevel(val characterClass: CharacterClass, val level: Int, val subclass: Subclass? = null)

/** A subclass as the server sends it: the app doesn't list them, it uses [id] and [name] as they come. */
data class Subclass(val id: String, val name: String)

/** Catalog entry of a class: the subclasses it offers and the class level to choose one. */
data class ClassInfo(val characterClass: CharacterClass, val subclassLevel: Int, val subclasses: List<Subclass>)
