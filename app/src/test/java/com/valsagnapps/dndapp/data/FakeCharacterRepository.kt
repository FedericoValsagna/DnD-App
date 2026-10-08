package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillValue
import com.valsagnapps.dndapp.domain.Subclass
import kotlinx.coroutines.CompletableDeferred

/**
 * In-memory repository for ViewModel tests. Set [failWith] to make every call fail, or
 * [gate] to suspend calls until the test completes it (to observe loading states).
 */
class FakeCharacterRepository(characters: List<Character> = emptyList()) : CharacterRepository {
    val characters = characters.toMutableList()
    val created = mutableListOf<NewCharacter>()
    val skillUpdates = mutableListOf<Pair<String, Map<Skill, Proficiency>>>()
    val classUpdates = mutableListOf<Pair<String, List<ClassLevel>>>()
    val hitPointUpdates = mutableListOf<Pair<String, Int>>()

    /** Each one is (id, class, subclass id). */
    val subclassUpdates = mutableListOf<Triple<String, CharacterClass, String?>>()

    /** What [listClasses] returns. Empty by default, as if the server had no catalog. */
    var classCatalog: List<ClassInfo> = emptyList()
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
            level = character.classes.sumOf { it.level },
            proficiencyBonus = 2,
            abilities = character.abilityScores.mapValues { (_, score) -> AbilityScore(score, 0) },
            skills = skillValues(character.skills),
            passivePerception = 10,
            classes = character.classes,
            maxHitPoints = character.maxHitPoints,
        ).also { characters += it }
    }

    /** Bonuses are fake (proficiency multiplier only): the real ones come from the server. */
    override suspend fun updateSkills(id: String, skills: Map<Skill, Proficiency>): RepositoryResult<Character> =
        respond {
            skillUpdates += id to skills
            val index = characters.indexOfFirst { it.id == id }
            characters[index].copy(skills = skillValues(skills)).also { characters[index] = it }
        }

    /** Like the server: keeps the subclass of the classes that stay and still have the level for it. */
    override suspend fun updateClasses(id: String, classes: List<ClassLevel>): RepositoryResult<Character> = respond {
        classUpdates += id to classes
        update(id) { character ->
            val kept = classes.map { new ->
                val old = character.classes.find { it.characterClass == new.characterClass }
                val subclassLevel = classCatalog.find { it.characterClass == new.characterClass }?.subclassLevel
                new.copy(subclass = old?.subclass?.takeIf { subclassLevel != null && new.level >= subclassLevel })
            }
            character.copy(classes = kept, level = classes.sumOf { it.level })
        }
    }

    /** Takes the subclass name from [classCatalog]. */
    override suspend fun updateSubclass(
        id: String,
        characterClass: CharacterClass,
        subclassId: String?,
    ): RepositoryResult<Character> = respond {
        subclassUpdates += Triple(id, characterClass, subclassId)
        val subclass = subclassId?.let { subclassId ->
            classCatalog.flatMap { it.subclasses }.find { it.id == subclassId } ?: Subclass(subclassId, subclassId)
        }
        update(id) { character ->
            character.copy(
                classes = character.classes.map {
                    if (it.characterClass == characterClass) it.copy(subclass = subclass) else it
                },
            )
        }
    }

    override suspend fun updateMaxHitPoints(id: String, maxHitPoints: Int): RepositoryResult<Character> = respond {
        hitPointUpdates += id to maxHitPoints
        update(id) { it.copy(maxHitPoints = maxHitPoints) }
    }

    override suspend fun listClasses(): RepositoryResult<List<ClassInfo>> = respond { classCatalog }

    private fun update(id: String, change: (Character) -> Character): Character {
        val index = characters.indexOfFirst { it.id == id }
        return change(characters[index]).also { characters[index] = it }
    }

    private fun skillValues(skills: Map<Skill, Proficiency>) = Skill.entries.associateWith {
        val proficiency = skills[it] ?: Proficiency.NONE
        SkillValue(proficiency, proficiency.ordinal)
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
    skills = Skill.entries.associateWith { SkillValue(Proficiency.NONE, 0) } +
        (Skill.PERCEPTION to SkillValue(Proficiency.PROFICIENT, 4)),
    passivePerception = 14,
    classes = listOf(ClassLevel(CharacterClass.FIGHTER, level)),
    maxHitPoints = 44,
)
