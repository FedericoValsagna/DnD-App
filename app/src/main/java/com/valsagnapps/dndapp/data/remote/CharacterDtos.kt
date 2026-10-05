package com.valsagnapps.dndapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val id: String,
    val name: String,
    val level: Int,
    val proficiencyBonus: Int,
    /** Keyed by ability name in upper case, e.g. "STRENGTH". */
    val abilities: Map<String, AbilityDto>,
)

@Serializable
data class AbilityDto(val score: Int, val modifier: Int)

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val level: Int,
    val abilityScores: AbilityScoresDto,
)

@Serializable
data class AbilityScoresDto(
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
)

/** Error body (`application/problem+json`, RFC 9457). */
@Serializable
data class ProblemDetailDto(
    val status: Int? = null,
    val title: String? = null,
    val detail: String? = null,
)
