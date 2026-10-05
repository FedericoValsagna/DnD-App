package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterApi
import com.valsagnapps.dndapp.data.remote.CharacterDto
import com.valsagnapps.dndapp.data.remote.CreateCharacterRequest
import com.valsagnapps.dndapp.data.remote.UpdateSkillsRequest

/** Each call runs its configured response: return a DTO or throw what Retrofit would throw. */
class FakeCharacterApi : CharacterApi {
    var listResponse: () -> List<CharacterDto> = { emptyList() }
    var getResponse: (id: String) -> CharacterDto = { error("not configured") }
    var createResponse: (CreateCharacterRequest) -> CharacterDto = { error("not configured") }
    var updateSkillsResponse: (id: String, UpdateSkillsRequest) -> CharacterDto = { _, _ -> error("not configured") }

    val createdRequests = mutableListOf<CreateCharacterRequest>()
    val updateSkillsRequests = mutableListOf<Pair<String, UpdateSkillsRequest>>()

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
}
