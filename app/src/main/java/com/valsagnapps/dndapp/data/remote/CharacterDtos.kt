package com.valsagnapps.dndapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val id: String,
    val name: String,
    val level: Int,
    val proficiencyBonus: Int,
    /** Keyed by ability name in upper case, e.g. "STRENGTH". */
    val abilities: Map<String, AbilityDto>,
    /** Keyed by skill name in upper case, e.g. "SLEIGHT_OF_HAND". Defaults in case the server doesn't send them. */
    val skills: Map<String, SkillDto> = emptyMap(),
    val passivePerception: Int? = null,
    val classes: List<ClassLevelDto> = emptyList(),
    val maxHitPoints: Int? = null,
    val hitDice: List<HitDiceDto> = emptyList(),
    /** Keyed by ability name in upper case. */
    val savingThrows: Map<String, SavingThrowDto> = emptyMap(),
)

/** [characterClass] e.g. "CLERIC". */
@Serializable
data class ClassLevelDto(@SerialName("class") val characterClass: String, val level: Int, val hitDie: Int? = null)

@Serializable
data class HitDiceDto(val die: Int, val count: Int)

@Serializable
data class SavingThrowDto(val proficiency: String, val bonus: Int)

@Serializable
data class AbilityDto(val score: Int, val modifier: Int)

/** [ability] e.g. "DEXTERITY"; [proficiency] is "NONE", "PROFICIENT" or "EXPERTISE". */
@Serializable
data class SkillDto(val ability: String, val proficiency: String, val bonus: Int)

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val classes: List<ClassLevelRequest>,
    val maxHitPoints: Int,
    val abilityScores: AbilityScoresDto,
    /** Only skills with proficiency; missing ones are NONE. */
    val skills: Map<String, String> = emptyMap(),
)

@Serializable
data class ClassLevelRequest(@SerialName("class") val characterClass: String, val level: Int)

/** Replaces all the proficiencies: missing skills are NONE. */
@Serializable
data class UpdateSkillsRequest(val skills: Map<String, String>)

/** Replaces all the classes; the first one is the starting class. */
@Serializable
data class UpdateClassesRequest(val classes: List<ClassLevelRequest>)

@Serializable
data class UpdateHitPointsRequest(val maxHitPoints: Int)

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
data class ProblemDetailDto(val status: Int? = null, val title: String? = null, val detail: String? = null)
