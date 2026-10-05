package com.valsagnapps.dndapp.domain

/** Data needed to create a character. Must satisfy [CharacterRules] before being sent. */
data class NewCharacter(
    val name: String,
    /** One class for now; the server already accepts several (multiclass). */
    val classes: List<ClassLevel>,
    val maxHitPoints: Int,
    val abilityScores: Map<Ability, Int>,
    val skills: Map<Skill, Proficiency> = emptyMap(),
)
