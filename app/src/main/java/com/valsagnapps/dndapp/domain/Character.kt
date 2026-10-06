package com.valsagnapps.dndapp.domain

/** A character as returned by the server, with the derived values it computes. */
data class Character(
    val id: String,
    val name: String,
    val level: Int,
    val proficiencyBonus: Int,
    val abilities: Map<Ability, AbilityScore>,
    val skills: Map<Skill, SkillValue> = emptyMap(),
    /** Null if the server didn't send it. */
    val passivePerception: Int? = null,
    /** In order: the first one is the starting class. Empty if the server didn't send them. */
    val classes: List<ClassLevel> = emptyList(),
    /** Null if the server didn't send it. */
    val maxHitPoints: Int? = null,
    /** Grouped by die, from largest to smallest. */
    val hitDice: List<HitDice> = emptyList(),
    val savingThrows: Map<Ability, SavingThrow> = emptyMap(),
    /** Skills each class offers, in the order of [classes]. Empty if the server didn't send them. */
    val skillChoices: Map<CharacterClass, SkillChoice> = emptyMap(),
    /** Null if the server didn't send them. */
    val proficiencies: Proficiencies? = null,
) {
    /** Proficiency in each skill; skills the server didn't send count as [Proficiency.NONE]. */
    val skillProficiencies: Map<Skill, Proficiency>
        get() = Skill.entries.associateWith { skills[it]?.proficiency ?: Proficiency.NONE }

    /** Skills suggested by some class. Classes that allow any skill don't count: they'd mark them all. */
    val classSkills: Set<Skill>
        get() = skillChoices.values.filterNot { it.isAnySkill }.flatMap { it.options }.toSet()
}

data class AbilityScore(val score: Int, val modifier: Int)

data class SkillValue(val proficiency: Proficiency, val bonus: Int)

data class HitDice(val die: Int, val count: Int)

data class SavingThrow(val proficiency: Proficiency, val bonus: Int)
