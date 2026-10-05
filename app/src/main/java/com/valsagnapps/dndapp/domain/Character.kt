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
) {
    /** Proficiency in each skill; skills the server didn't send count as [Proficiency.NONE]. */
    val skillProficiencies: Map<Skill, Proficiency>
        get() = Skill.entries.associateWith { skills[it]?.proficiency ?: Proficiency.NONE }
}

data class AbilityScore(val score: Int, val modifier: Int)

data class SkillValue(val proficiency: Proficiency, val bonus: Int)
