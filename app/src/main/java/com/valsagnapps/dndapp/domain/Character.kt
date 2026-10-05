package com.valsagnapps.dndapp.domain

/** A character as returned by the server, with the derived values it computes. */
data class Character(
    val id: String,
    val name: String,
    val level: Int,
    val proficiencyBonus: Int,
    val abilities: Map<Ability, AbilityScore>,
)

data class AbilityScore(val score: Int, val modifier: Int)
