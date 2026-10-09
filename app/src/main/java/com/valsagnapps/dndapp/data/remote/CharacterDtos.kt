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
    val proficiencies: ProficienciesDto? = null,
)

/** [characterClass] e.g. "CLERIC". */
@Serializable
data class ClassLevelDto(
    @SerialName("class") val characterClass: String,
    val level: Int,
    val hitDie: Int? = null,
    val skillChoices: ChoiceDto? = null,
    val subclassLevel: Int? = null,
    val subclass: SubclassDto? = null,
    val features: List<ClassFeatureDto> = emptyList(),
)

/** [id] e.g. "CLERIC_CHANNEL_DIVINITY"; [srdText] is null for features that aren't SRD. */
@Serializable
data class ClassFeatureDto(
    val id: String,
    val name: String,
    val level: Int,
    val summary: String,
    val srdText: String? = null,
)

/** [id] e.g. "LIFE"; [name] e.g. "Life Domain". */
@Serializable
data class SubclassDto(val id: String, val name: String)

/** Entry of the class catalog (`GET /api/v1/classes`). */
@Serializable
data class ClassDto(
    @SerialName("class") val characterClass: String,
    val subclassLevel: Int,
    val subclasses: List<SubclassDto> = emptyList(),
)

/** [options] are enum names: skills (e.g. "ATHLETICS") or tool groups (e.g. "MUSICAL_INSTRUMENT"). */
@Serializable
data class ChoiceDto(val count: Int, val options: List<String>)

/** Enum names, e.g. "SHIELDS", "MARTIAL", "THIEVES_TOOLS". */
@Serializable
data class ProficienciesDto(
    val armor: List<String> = emptyList(),
    val weapons: List<String> = emptyList(),
    val tools: List<String> = emptyList(),
    val toolChoices: List<ChoiceDto> = emptyList(),
)

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

/** [subclass] is a subclass id, or null to remove it. */
@Serializable
data class UpdateSubclassRequest(val subclass: String?)

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
