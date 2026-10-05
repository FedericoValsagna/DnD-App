package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter
import kotlinx.coroutines.CompletableDeferred

/**
 * In-memory repository for ViewModel tests. Set [failWith] to make every call fail, or
 * [gate] to suspend calls until the test completes it (to observe loading states).
 */
class FakeCharacterRepository(characters: List<Character> = emptyList()) : CharacterRepository {
    val characters = characters.toMutableList()
    val created = mutableListOf<NewCharacter>()
    var failWith: RepositoryError? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun list(): RepositoryResult<List<Character>> = respond { characters.toList() }

    override suspend fun get(id: String): RepositoryResult<Character> {
        gate?.await()
        val error = failWith
        val character = characters.find { it.id == id }
        return when {
            error != null -> RepositoryResult.Failure(error)
            character == null -> RepositoryResult.Failure(RepositoryError.NotFound)
            else -> RepositoryResult.Success(character)
        }
    }

    override suspend fun create(character: NewCharacter): RepositoryResult<Character> = respond {
        created += character
        Character(
            id = "created-${created.size}",
            name = character.name,
            level = character.level,
            proficiencyBonus = 2,
            abilities = character.abilityScores.mapValues { (_, score) -> AbilityScore(score, 0) },
        ).also { characters += it }
    }

    private suspend fun <T> respond(value: () -> T): RepositoryResult<T> {
        gate?.await()
        failWith?.let { return RepositoryResult.Failure(it) }
        return RepositoryResult.Success(value())
    }
}

fun sampleCharacter(id: String = "1", name: String = "Tordek", level: Int = 5) = Character(
    id = id,
    name = name,
    level = level,
    proficiencyBonus = 3,
    abilities = mapOf(
        Ability.STRENGTH to AbilityScore(16, 3),
        Ability.DEXTERITY to AbilityScore(12, 1),
        Ability.CONSTITUTION to AbilityScore(15, 2),
        Ability.INTELLIGENCE to AbilityScore(10, 0),
        Ability.WISDOM to AbilityScore(13, 1),
        Ability.CHARISMA to AbilityScore(8, -1),
    ),
)
