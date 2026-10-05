package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.data.remote.AbilityDto
import com.valsagnapps.dndapp.data.remote.CharacterDto
import com.valsagnapps.dndapp.data.remote.ClassLevelRequest
import com.valsagnapps.dndapp.data.remote.UpdateClassesRequest
import com.valsagnapps.dndapp.data.remote.UpdateHitPointsRequest
import com.valsagnapps.dndapp.data.remote.toDomain
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RemoteCharacterRepositoryTest {

    private val api = FakeCharacterApi()
    private val repository = RemoteCharacterRepository(api, Json { ignoreUnknownKeys = true })

    private val tordekDto = CharacterDto(
        id = "1",
        name = "Tordek",
        level = 5,
        proficiencyBonus = 3,
        abilities = mapOf("STRENGTH" to AbilityDto(16, 3)),
    )

    @Test
    fun `returns the characters the server lists`() = runTest {
        api.listResponse = { listOf(tordekDto) }

        assertEquals(RepositoryResult.Success(listOf(tordekDto.toDomain())), repository.list())
    }

    @Test
    fun `returns not found when the server answers 404`() = runTest {
        api.getResponse = { throw httpError(404, """{"status":404,"detail":"Character 1 not found"}""") }

        assertEquals(RepositoryResult.Failure(RepositoryError.NotFound), repository.get("1"))
    }

    @Test
    fun `returns the problem detail when the server rejects a request`() = runTest {
        api.createResponse = {
            throw httpError(400, """{"status":400,"title":"Bad Request","detail":"Invalid request content."}""")
        }

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Server(400, "Invalid request content.")),
            repository.create(newCharacter()),
        )
    }

    @Test
    fun `returns a server error without detail when the error body is not problem json`() = runTest {
        api.listResponse = { throw httpError(500, "<html>oops</html>") }

        assertEquals(RepositoryResult.Failure(RepositoryError.Server(500, null)), repository.list())
    }

    @Test
    fun `returns network error when the server cannot be reached`() = runTest {
        api.listResponse = { throw IOException("connection refused") }

        assertEquals(RepositoryResult.Failure(RepositoryError.Network), repository.list())
    }

    @Test
    fun `returns invalid response when the body cannot be parsed`() = runTest {
        api.listResponse = { throw SerializationException("bad json") }

        assertEquals(RepositoryResult.Failure(RepositoryError.InvalidResponse), repository.list())
    }

    @Test
    fun `sends the new character to the server and returns the created one`() = runTest {
        api.createResponse = { tordekDto }

        val result = repository.create(newCharacter())

        val request = api.createdRequests.single()
        assertEquals("Tordek", request.name)
        assertEquals(16, request.abilityScores.strength)
        assertEquals(RepositoryResult.Success(tordekDto.toDomain()), result)
    }

    @Test
    fun `sends the skills of a character and returns the updated one`() = runTest {
        api.updateSkillsResponse = { _, _ -> tordekDto }

        val result = repository.updateSkills("1", mapOf(Skill.STEALTH to Proficiency.EXPERTISE))

        val (id, request) = api.updateSkillsRequests.single()
        assertEquals("1", id)
        assertEquals(mapOf("STEALTH" to "EXPERTISE"), request.skills)
        assertEquals(RepositoryResult.Success(tordekDto.toDomain()), result)
    }

    @Test
    fun `returns not found when updating skills of a missing character`() = runTest {
        api.updateSkillsResponse = { _, _ -> throw httpError(404, """{"status":404}""") }

        assertEquals(
            RepositoryResult.Failure(RepositoryError.NotFound),
            repository.updateSkills("missing", emptyMap()),
        )
    }

    @Test
    fun `sends the classes of a character and returns the updated one`() = runTest {
        api.updateClassesResponse = { _, _ -> tordekDto }

        val result = repository.updateClasses(
            "1",
            listOf(ClassLevel(CharacterClass.FIGHTER, 3), ClassLevel(CharacterClass.CLERIC, 2)),
        )

        val (id, request) = api.updateClassesRequests.single()
        assertEquals("1", id)
        assertEquals(
            UpdateClassesRequest(listOf(ClassLevelRequest("FIGHTER", 3), ClassLevelRequest("CLERIC", 2))),
            request,
        )
        assertEquals(RepositoryResult.Success(tordekDto.toDomain()), result)
    }

    @Test
    fun `returns the server error when the classes are rejected`() = runTest {
        api.updateClassesResponse = { _, _ ->
            throw httpError(400, """{"status":400,"detail":"classes must not be repeated"}""")
        }

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Server(400, "classes must not be repeated")),
            repository.updateClasses("1", listOf(ClassLevel(CharacterClass.ROGUE, 1))),
        )
    }

    @Test
    fun `sends the max hit points of a character and returns the updated one`() = runTest {
        api.updateHitPointsResponse = { _, _ -> tordekDto }

        val result = repository.updateMaxHitPoints("1", 47)

        assertEquals(listOf("1" to UpdateHitPointsRequest(47)), api.updateHitPointsRequests)
        assertEquals(RepositoryResult.Success(tordekDto.toDomain()), result)
    }

    @Test
    fun `returns not found when updating hit points of a missing character`() = runTest {
        api.updateHitPointsResponse = { _, _ -> throw httpError(404, """{"status":404}""") }

        assertEquals(RepositoryResult.Failure(RepositoryError.NotFound), repository.updateMaxHitPoints("missing", 10))
    }

    @Test
    fun `logs the exception behind a failure`() = runTest {
        val logged = mutableListOf<Throwable>()
        val repository = RemoteCharacterRepository(api, Json, logFailure = { logged += it })
        val exception = IOException("connection refused")
        api.listResponse = { throw exception }

        repository.list()

        assertEquals(listOf(exception), logged)
    }

    private fun newCharacter() = NewCharacter(
        name = "Tordek",
        classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5)),
        maxHitPoints = 44,
        abilityScores = Ability.entries.associateWith { 10 } + (Ability.STRENGTH to 16),
    )

    private fun httpError(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/problem+json".toMediaType())),
    )
}
