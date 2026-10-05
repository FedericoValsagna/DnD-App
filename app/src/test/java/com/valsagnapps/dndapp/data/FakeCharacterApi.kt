package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterApi
import com.valsagnapps.dndapp.data.remote.CharacterDto
import com.valsagnapps.dndapp.data.remote.CreateCharacterRequest

/** Each call runs its configured response: return a DTO or throw what Retrofit would throw. */
class FakeCharacterApi : CharacterApi {
    var listResponse: () -> List<CharacterDto> = { emptyList() }
    var getResponse: (id: String) -> CharacterDto = { error("not configured") }
    var createResponse: (CreateCharacterRequest) -> CharacterDto = { error("not configured") }

    val createdRequests = mutableListOf<CreateCharacterRequest>()

    override suspend fun list(): List<CharacterDto> = listResponse()

    override suspend fun get(id: String): CharacterDto = getResponse(id)

    override suspend fun create(request: CreateCharacterRequest): CharacterDto {
        createdRequests += request
        return createResponse(request)
    }
}
