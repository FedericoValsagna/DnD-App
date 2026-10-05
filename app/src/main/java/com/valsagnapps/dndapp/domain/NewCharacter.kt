package com.valsagnapps.dndapp.domain

/** Data needed to create a character. Must satisfy [CharacterRules] before being sent. */
data class NewCharacter(
    val name: String,
    val level: Int,
    val abilityScores: Map<Ability, Int>,
    val skills: Map<Skill, Proficiency> = emptyMap(),
)
