package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillValue

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    level = level,
    proficiencyBonus = proficiencyBonus,
    // Abilities and skills the app doesn't know about are ignored instead of failing the whole response.
    abilities = abilities.mapNotNull { (key, dto) ->
        Ability.entries.find { it.name == key }?.let { it to AbilityScore(dto.score, dto.modifier) }
    }.toMap(),
    skills = skills.mapNotNull { (key, dto) ->
        val skill = Skill.entries.find { it.name == key }
        val proficiency = Proficiency.entries.find { it.name == dto.proficiency }
        if (skill == null || proficiency == null) null else skill to SkillValue(proficiency, dto.bonus)
    }.toMap(),
    passivePerception = passivePerception,
)

fun NewCharacter.toRequest(): CreateCharacterRequest = CreateCharacterRequest(
    name = name,
    level = level,
    abilityScores = AbilityScoresDto(
        strength = abilityScores.getValue(Ability.STRENGTH),
        dexterity = abilityScores.getValue(Ability.DEXTERITY),
        constitution = abilityScores.getValue(Ability.CONSTITUTION),
        intelligence = abilityScores.getValue(Ability.INTELLIGENCE),
        wisdom = abilityScores.getValue(Ability.WISDOM),
        charisma = abilityScores.getValue(Ability.CHARISMA),
    ),
    skills = skills.toSkillsDto(),
)

fun Map<Skill, Proficiency>.toUpdateSkillsRequest(): UpdateSkillsRequest = UpdateSkillsRequest(toSkillsDto())

private fun Map<Skill, Proficiency>.toSkillsDto(): Map<String, String> =
    filterValues { it != Proficiency.NONE }.entries.associate { (skill, proficiency) -> skill.name to proficiency.name }
