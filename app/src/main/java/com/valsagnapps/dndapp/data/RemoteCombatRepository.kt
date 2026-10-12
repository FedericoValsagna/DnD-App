package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CombatApi
import com.valsagnapps.dndapp.data.remote.DamageRequest
import com.valsagnapps.dndapp.data.remote.DeathSavesDto
import com.valsagnapps.dndapp.data.remote.HealingRequest
import com.valsagnapps.dndapp.data.remote.HitDiceSpendDto
import com.valsagnapps.dndapp.data.remote.ReviveRequest
import com.valsagnapps.dndapp.data.remote.ShortRestRequest
import com.valsagnapps.dndapp.data.remote.UpdateArmorClassRequest
import com.valsagnapps.dndapp.data.remote.UpdateResourceRequest
import com.valsagnapps.dndapp.data.remote.UpdateSpeedRequest
import com.valsagnapps.dndapp.data.remote.UpdateTemporaryHitPointsRequest
import com.valsagnapps.dndapp.data.remote.toDomain
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.DeathSaves
import kotlinx.serialization.json.Json

/** @param logFailure receives every exception turned into a [RepositoryError], as in [RemoteCharacterRepository]. */
class RemoteCombatRepository(private val api: CombatApi, json: Json, logFailure: (Throwable) -> Unit = {}) :
    CombatRepository {

    private val call = RemoteCall(json, logFailure)

    override suspend fun takeDamage(id: String, amount: Int, critical: Boolean): RepositoryResult<Character> =
        call { api.takeDamage(id, DamageRequest(amount, critical)).toDomain() }

    override suspend fun heal(id: String, amount: Int): RepositoryResult<Character> =
        call { api.heal(id, HealingRequest(amount)).toDomain() }

    override suspend fun setTemporaryHitPoints(id: String, temporaryHitPoints: Int): RepositoryResult<Character> =
        call { api.setTemporaryHitPoints(id, UpdateTemporaryHitPointsRequest(temporaryHitPoints)).toDomain() }

    override suspend fun updateDeathSaves(id: String, deathSaves: DeathSaves): RepositoryResult<Character> =
        call { api.updateDeathSaves(id, DeathSavesDto(deathSaves.successes, deathSaves.failures)).toDomain() }

    override suspend fun revive(id: String, hitPoints: Int): RepositoryResult<Character> =
        call { api.revive(id, ReviveRequest(hitPoints)).toDomain() }

    override suspend fun updateArmorClassOverride(id: String, armorClassOverride: Int?): RepositoryResult<Character> =
        call { api.updateArmorClass(id, UpdateArmorClassRequest(armorClassOverride)).toDomain() }

    override suspend fun updateBaseSpeed(id: String, baseSpeed: Int): RepositoryResult<Character> =
        call { api.updateSpeed(id, UpdateSpeedRequest(baseSpeed)).toDomain() }

    override suspend fun updateResourceUsed(id: String, resource: String, used: Int): RepositoryResult<Character> =
        call { api.updateResource(id, resource, UpdateResourceRequest(used)).toDomain() }

    override suspend fun shortRest(id: String, hitDice: Map<Int, Int>, roll: Int): RepositoryResult<Character> = call {
        val spent = hitDice.filterValues { it > 0 }.map { (die, count) -> HitDiceSpendDto(die, count) }
        api.shortRest(id, ShortRestRequest(spent, roll)).toDomain()
    }

    override suspend fun longRest(id: String): RepositoryResult<Character> = call { api.longRest(id).toDomain() }
}
