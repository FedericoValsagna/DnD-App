package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterDto
import com.valsagnapps.dndapp.data.remote.CombatApi
import com.valsagnapps.dndapp.data.remote.DamageRequest
import com.valsagnapps.dndapp.data.remote.DeathSavesDto
import com.valsagnapps.dndapp.data.remote.HealingRequest
import com.valsagnapps.dndapp.data.remote.ReviveRequest
import com.valsagnapps.dndapp.data.remote.UpdateArmorClassRequest
import com.valsagnapps.dndapp.data.remote.UpdateSpeedRequest
import com.valsagnapps.dndapp.data.remote.UpdateTemporaryHitPointsRequest

/** Records every request as (id, body) and answers with [response], which can also throw what Retrofit would. */
class FakeCombatApi : CombatApi {
    var response: (id: String) -> CharacterDto = { error("not configured") }
    val requests = mutableListOf<Pair<String, Any>>()

    override suspend fun takeDamage(id: String, request: DamageRequest) = respond(id, request)

    override suspend fun heal(id: String, request: HealingRequest) = respond(id, request)

    override suspend fun setTemporaryHitPoints(id: String, request: UpdateTemporaryHitPointsRequest) =
        respond(id, request)

    override suspend fun updateDeathSaves(id: String, request: DeathSavesDto) = respond(id, request)

    override suspend fun revive(id: String, request: ReviveRequest) = respond(id, request)

    override suspend fun updateArmorClass(id: String, request: UpdateArmorClassRequest) = respond(id, request)

    override suspend fun updateSpeed(id: String, request: UpdateSpeedRequest) = respond(id, request)

    private fun respond(id: String, request: Any): CharacterDto {
        requests += id to request
        return response(id)
    }
}
