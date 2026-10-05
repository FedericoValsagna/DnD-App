package com.valsagnapps.dndapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CharacterApi {

    @GET("api/v1/characters")
    suspend fun list(): List<CharacterDto>

    @GET("api/v1/characters/{id}")
    suspend fun get(@Path("id") id: String): CharacterDto

    @POST("api/v1/characters")
    suspend fun create(@Body request: CreateCharacterRequest): CharacterDto
}
