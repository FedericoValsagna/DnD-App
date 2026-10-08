package com.valsagnapps.dndapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CharacterApi {

    @GET("api/v1/characters")
    suspend fun list(): List<CharacterDto>

    @GET("api/v1/characters/{id}")
    suspend fun get(@Path("id") id: String): CharacterDto

    @POST("api/v1/characters")
    suspend fun create(@Body request: CreateCharacterRequest): CharacterDto

    @PUT("api/v1/characters/{id}/skills")
    suspend fun updateSkills(@Path("id") id: String, @Body request: UpdateSkillsRequest): CharacterDto

    @PUT("api/v1/characters/{id}/classes")
    suspend fun updateClasses(@Path("id") id: String, @Body request: UpdateClassesRequest): CharacterDto

    @PUT("api/v1/characters/{id}/classes/{class}/subclass")
    suspend fun updateSubclass(
        @Path("id") id: String,
        @Path("class") characterClass: String,
        @Body request: UpdateSubclassRequest,
    ): CharacterDto

    @PUT("api/v1/characters/{id}/hit-points")
    suspend fun updateHitPoints(@Path("id") id: String, @Body request: UpdateHitPointsRequest): CharacterDto

    @GET("api/v1/classes")
    suspend fun listClasses(): List<ClassDto>
}
