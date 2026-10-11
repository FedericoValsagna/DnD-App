package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.CharacterDto
import com.valsagnapps.dndapp.data.remote.DamageRequest
import com.valsagnapps.dndapp.data.remote.DeathSavesDto
import com.valsagnapps.dndapp.data.remote.HealingRequest
import com.valsagnapps.dndapp.data.remote.ReviveRequest
import com.valsagnapps.dndapp.data.remote.UpdateArmorClassRequest
import com.valsagnapps.dndapp.data.remote.UpdateSpeedRequest
import com.valsagnapps.dndapp.data.remote.UpdateTemporaryHitPointsRequest
import com.valsagnapps.dndapp.data.remote.toDomain
import com.valsagnapps.dndapp.domain.DeathSaves
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RemoteCombatRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val api = FakeCombatApi()
    private val repository = RemoteCombatRepository(api, json)

    private val aldricDto = CharacterDto(
        id = "1",
        name = "Aldric",
        level = 5,
        proficiencyBonus = 3,
        abilities = emptyMap(),
        maxHitPoints = 44,
        currentHitPoints = 9,
    )

    init {
        api.response = { aldricDto }
    }

    @Test
    fun `each action sends its request and returns the character the server leaves`() = runTest {
        val expected = RepositoryResult.Success(aldricDto.toDomain())

        assertEquals(expected, repository.takeDamage("1", 8, critical = true))
        assertEquals(expected, repository.heal("1", 5))
        assertEquals(expected, repository.setTemporaryHitPoints("1", 7))
        assertEquals(expected, repository.updateDeathSaves("1", DeathSaves(successes = 1, failures = 2)))
        assertEquals(expected, repository.revive("1", 1))
        assertEquals(expected, repository.updateArmorClassOverride("1", 18))
        assertEquals(expected, repository.updateBaseSpeed("1", 25))

        assertEquals(
            listOf(
                "1" to DamageRequest(8, critical = true),
                "1" to HealingRequest(5),
                "1" to UpdateTemporaryHitPointsRequest(7),
                "1" to DeathSavesDto(successes = 1, failures = 2),
                "1" to ReviveRequest(1),
                "1" to UpdateArmorClassRequest(18),
                "1" to UpdateSpeedRequest(25),
            ),
            api.requests,
        )
    }

    @Test
    fun `removing the armor class override sends null`() {
        assertEquals(
            """{"armorClassOverride":null}""",
            json.encodeToString(UpdateArmorClassRequest.serializer(), UpdateArmorClassRequest(null)),
        )
    }

    @Test
    fun `returns the problem detail when the state doesn't allow the action`() = runTest {
        api.response = {
            throw HttpException(
                Response.error<Any>(
                    409,
                    """{"status":409,"detail":"a dead character can't be healed: revive it first"}"""
                        .toResponseBody("application/problem+json".toMediaType()),
                ),
            )
        }

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Server(409, "a dead character can't be healed: revive it first")),
            repository.heal("1", 5),
        )
    }
}
