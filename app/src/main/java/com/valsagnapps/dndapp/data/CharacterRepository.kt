package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill

interface CharacterRepository {
    suspend fun list(): RepositoryResult<List<Character>>
    suspend fun get(id: String): RepositoryResult<Character>
    suspend fun create(character: NewCharacter): RepositoryResult<Character>

    /** Replaces all the character's skill proficiencies: skills not in [skills] end up as NONE. */
    suspend fun updateSkills(id: String, skills: Map<Skill, Proficiency>): RepositoryResult<Character>
}
