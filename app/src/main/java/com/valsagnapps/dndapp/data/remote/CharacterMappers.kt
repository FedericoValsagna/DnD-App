package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    level = level,
    proficiencyBonus = proficiencyBonus,
    // Abilities the app doesn't know about are ignored instead of failing the whole response.
    abilities = abilities.mapNotNull { (key, dto) ->
        Ability.entries.find { it.name == key }?.let { it to AbilityScore(dto.score, dto.modifier) }
    }.toMap(),
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
)
