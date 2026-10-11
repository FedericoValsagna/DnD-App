package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill

interface CharacterRepository {
    suspend fun list(): RepositoryResult<List<Character>>
    suspend fun get(id: String): RepositoryResult<Character>
    suspend fun create(character: NewCharacter): RepositoryResult<Character>

    /** Replaces all the character's skill proficiencies: skills not in [skills] end up as NONE. */
    suspend fun updateSkills(id: String, skills: Map<Skill, Proficiency>): RepositoryResult<Character>

    /**
     * Replaces all the character's classes; the first one is the starting class. The server keeps the subclass
     * of the classes that stay and removes it if the class changes or drops below the level to have one.
     */
    suspend fun updateClasses(id: String, classes: List<ClassLevel>): RepositoryResult<Character>

    /** Chooses the subclass of one of the character's classes; a null [subclassId] removes it. */
    suspend fun updateSubclass(
        id: String,
        characterClass: CharacterClass,
        subclassId: String?,
    ): RepositoryResult<Character>

    suspend fun updateMaxHitPoints(id: String, maxHitPoints: Int): RepositoryResult<Character>

    suspend fun updateName(id: String, name: String): RepositoryResult<Character>

    /** Replaces the six ability scores: [scores] must have all of them. */
    suspend fun updateAbilityScores(id: String, scores: Map<Ability, Int>): RepositoryResult<Character>

    /** Deletes the character with everything it has. */
    suspend fun delete(id: String): RepositoryResult<Unit>

    /** The class catalog, with the subclasses of each class. */
    suspend fun listClasses(): RepositoryResult<List<ClassInfo>>
}
