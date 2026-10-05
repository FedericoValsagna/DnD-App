package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter

interface CharacterRepository {
    suspend fun list(): RepositoryResult<List<Character>>
    suspend fun get(id: String): RepositoryResult<Character>
    suspend fun create(character: NewCharacter): RepositoryResult<Character>
}
