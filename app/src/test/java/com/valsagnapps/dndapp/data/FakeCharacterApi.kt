package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterApi
import com.valsagnapps.dndapp.data.remote.CharacterDto
import com.valsagnapps.dndapp.data.remote.ClassDto
import com.valsagnapps.dndapp.data.remote.CreateCharacterRequest
import com.valsagnapps.dndapp.data.remote.UpdateClassesRequest
import com.valsagnapps.dndapp.data.remote.UpdateHitPointsRequest
import com.valsagnapps.dndapp.data.remote.UpdateSkillsRequest
import com.valsagnapps.dndapp.data.remote.UpdateSubclassRequest

/** Each call runs its configured response: return a DTO or throw what Retrofit would throw. */
class FakeCharacterApi : CharacterApi {
    var listResponse: () -> List<CharacterDto> = { emptyList() }
    var getResponse: (id: String) -> CharacterDto = { error("not configured") }
    var createResponse: (CreateCharacterRequest) -> CharacterDto = { error("not configured") }
    var updateSkillsResponse: (id: String, UpdateSkillsRequest) -> CharacterDto = { _, _ -> error("not configured") }

    val createdRequests = mutableListOf<CreateCharacterRequest>()
    val updateSkillsRequests = mutableListOf<Pair<String, UpdateSkillsRequest>>()
    var updateClassesResponse: (id: String, UpdateClassesRequest) -> CharacterDto = { _, _ -> error("not configured") }
    val updateClassesRequests = mutableListOf<Pair<String, UpdateClassesRequest>>()
    var updateHitPointsResponse: (id: String, UpdateHitPointsRequest) -> CharacterDto =
        { _, _ -> error("not configured") }
    val updateHitPointsRequests = mutableListOf<Pair<String, UpdateHitPointsRequest>>()
    var updateSubclassResponse: (id: String, characterClass: String, UpdateSubclassRequest) -> CharacterDto =
        { _, _, _ -> error("not configured") }

    /** Each one is (id, class, request). */
    val updateSubclassRequests = mutableListOf<Triple<String, String, UpdateSubclassRequest>>()
    var listClassesResponse: () -> List<ClassDto> = { emptyList() }

    override suspend fun list(): List<CharacterDto> = listResponse()

    override suspend fun get(id: String): CharacterDto = getResponse(id)

    override suspend fun create(request: CreateCharacterRequest): CharacterDto {
        createdRequests += request
        return createResponse(request)
    }

    override suspend fun updateSkills(id: String, request: UpdateSkillsRequest): CharacterDto {
        updateSkillsRequests += id to request
        return updateSkillsResponse(id, request)
    }

    override suspend fun updateClasses(id: String, request: UpdateClassesRequest): CharacterDto {
        updateClassesRequests += id to request
        return updateClassesResponse(id, request)
    }

    override suspend fun updateHitPoints(id: String, request: UpdateHitPointsRequest): CharacterDto {
        updateHitPointsRequests += id to request
        return updateHitPointsResponse(id, request)
    }

    override suspend fun updateSubclass(
        id: String,
        characterClass: String,
        request: UpdateSubclassRequest,
    ): CharacterDto {
        updateSubclassRequests += Triple(id, characterClass, request)
        return updateSubclassResponse(id, characterClass, request)
    }

    override suspend fun listClasses(): List<ClassDto> = listClassesResponse()
}
