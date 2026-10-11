package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterApi
import com.valsagnapps.dndapp.data.remote.UpdateHitPointsRequest
import com.valsagnapps.dndapp.data.remote.UpdateNameRequest
import com.valsagnapps.dndapp.data.remote.UpdateSubclassRequest
import com.valsagnapps.dndapp.data.remote.toDomain
import com.valsagnapps.dndapp.data.remote.toRequest
import com.valsagnapps.dndapp.data.remote.toUpdateAbilityScoresRequest
import com.valsagnapps.dndapp.data.remote.toUpdateClassesRequest
import com.valsagnapps.dndapp.data.remote.toUpdateSkillsRequest
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import kotlinx.serialization.json.Json

/**
 * @param logFailure receives every exception turned into a [RepositoryError], so it isn't lost
 * (the app sends it to Logcat).
 */
class RemoteCharacterRepository(private val api: CharacterApi, json: Json, logFailure: (Throwable) -> Unit = {}) :
    CharacterRepository {

    private val call = RemoteCall(json, logFailure)

    override suspend fun list(): RepositoryResult<List<Character>> = call { api.list().map { it.toDomain() } }

    override suspend fun get(id: String): RepositoryResult<Character> = call { api.get(id).toDomain() }

    override suspend fun create(character: NewCharacter): RepositoryResult<Character> =
        call { api.create(character.toRequest()).toDomain() }

    override suspend fun updateSkills(id: String, skills: Map<Skill, Proficiency>): RepositoryResult<Character> =
        call { api.updateSkills(id, skills.toUpdateSkillsRequest()).toDomain() }

    override suspend fun updateClasses(id: String, classes: List<ClassLevel>): RepositoryResult<Character> =
        call { api.updateClasses(id, classes.toUpdateClassesRequest()).toDomain() }

    override suspend fun updateSubclass(
        id: String,
        characterClass: CharacterClass,
        subclassId: String?,
    ): RepositoryResult<Character> =
        call { api.updateSubclass(id, characterClass.name, UpdateSubclassRequest(subclassId)).toDomain() }

    override suspend fun updateMaxHitPoints(id: String, maxHitPoints: Int): RepositoryResult<Character> =
        call { api.updateHitPoints(id, UpdateHitPointsRequest(maxHitPoints)).toDomain() }

    override suspend fun updateName(id: String, name: String): RepositoryResult<Character> =
        call { api.updateName(id, UpdateNameRequest(name)).toDomain() }

    override suspend fun updateAbilityScores(id: String, scores: Map<Ability, Int>): RepositoryResult<Character> =
        call { api.updateAbilityScores(id, scores.toUpdateAbilityScoresRequest()).toDomain() }

    override suspend fun delete(id: String): RepositoryResult<Unit> = call { api.delete(id) }

    override suspend fun listClasses(): RepositoryResult<List<ClassInfo>> = call { api.listClasses().toDomain() }
}
