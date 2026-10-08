package com.valsagnapps.dndapp.data

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

    /** The class catalog, with the subclasses of each class. */
    suspend fun listClasses(): RepositoryResult<List<ClassInfo>>
}
