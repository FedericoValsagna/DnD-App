package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability

/** Needs a score for each of the six abilities. */
fun Map<Ability, Int>.toAbilityScoresDto(): AbilityScoresDto = AbilityScoresDto(
    strength = getValue(Ability.STRENGTH),
    dexterity = getValue(Ability.DEXTERITY),
    constitution = getValue(Ability.CONSTITUTION),
    intelligence = getValue(Ability.INTELLIGENCE),
    wisdom = getValue(Ability.WISDOM),
    charisma = getValue(Ability.CHARISMA),
)

fun Map<Ability, Int>.toUpdateAbilityScoresRequest(): UpdateAbilityScoresRequest =
    UpdateAbilityScoresRequest(toAbilityScoresDto())
