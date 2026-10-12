package com.valsagnapps.dndapp.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Combat actions on a character. Each one returns the updated character; what the state doesn't allow is a 409. */
interface CombatApi {

    @POST("api/v1/characters/{id}/damage")
    suspend fun takeDamage(@Path("id") id: String, @Body request: DamageRequest): CharacterDto

    @POST("api/v1/characters/{id}/healing")
    suspend fun heal(@Path("id") id: String, @Body request: HealingRequest): CharacterDto

    @PUT("api/v1/characters/{id}/temporary-hit-points")
    suspend fun setTemporaryHitPoints(
        @Path("id") id: String,
        @Body request: UpdateTemporaryHitPointsRequest,
    ): CharacterDto

    @PUT("api/v1/characters/{id}/death-saves")
    suspend fun updateDeathSaves(@Path("id") id: String, @Body request: DeathSavesDto): CharacterDto

    @POST("api/v1/characters/{id}/revive")
    suspend fun revive(@Path("id") id: String, @Body request: ReviveRequest): CharacterDto

    @PUT("api/v1/characters/{id}/armor-class")
    suspend fun updateArmorClass(@Path("id") id: String, @Body request: UpdateArmorClassRequest): CharacterDto

    @PUT("api/v1/characters/{id}/speed")
    suspend fun updateSpeed(@Path("id") id: String, @Body request: UpdateSpeedRequest): CharacterDto

    @PUT("api/v1/characters/{id}/resources/{resource}")
    suspend fun updateResource(
        @Path("id") id: String,
        @Path("resource") resource: String,
        @Body request: UpdateResourceRequest,
    ): CharacterDto
}
