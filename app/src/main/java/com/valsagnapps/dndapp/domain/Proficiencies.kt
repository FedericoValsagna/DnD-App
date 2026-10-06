package com.valsagnapps.dndapp.domain

/** Armor, weapon and tool proficiencies of a character, computed by the server from its classes. */
data class Proficiencies(
    val armor: List<ArmorProficiency> = emptyList(),
    val weapons: List<WeaponProficiency> = emptyList(),
    val tools: List<ToolProficiency> = emptyList(),
    /** Tools the player picks (e.g. three musical instruments); which ones is not stored. */
    val toolChoices: List<ToolChoice> = emptyList(),
)

enum class ArmorProficiency {
    LIGHT,
    MEDIUM,
    HEAVY,
    SHIELDS,
}

/** A whole category ([SIMPLE], [MARTIAL]) or a single weapon, same as the server. */
enum class WeaponProficiency {
    SIMPLE,
    MARTIAL,
    CLUB,
    DAGGER,
    DART,
    JAVELIN,
    LIGHT_CROSSBOW,
    MACE,
    QUARTERSTAFF,
    SICKLE,
    SLING,
    SPEAR,
    HAND_CROSSBOW,
    LONGSWORD,
    RAPIER,
    SCIMITAR,
    SHORTSWORD,
}

enum class ToolProficiency {
    HERBALISM_KIT,
    THIEVES_TOOLS,
}

enum class ToolCategory {
    ARTISANS_TOOLS,
    MUSICAL_INSTRUMENT,
}

/** [count] tools from any of the [options] groups. */
data class ToolChoice(val count: Int, val options: List<ToolCategory>)

/** How many skills a class offers and from which ones. Only a suggestion: skills are marked freely. */
data class SkillChoice(val count: Int, val options: Set<Skill>) {
    /** Whether the class lets the player pick any skill (like the Bard). */
    val isAnySkill: Boolean
        get() = options.containsAll(Skill.entries)

    /** How many of the [options] have proficiency in [proficiencies]. */
    fun chosenIn(proficiencies: Map<Skill, Proficiency>): Int =
        options.count { (proficiencies[it] ?: Proficiency.NONE) != Proficiency.NONE }
}
